package tech.rayline.core.gui;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import tech.rayline.core.command.EmptyHandlerException;
import tech.rayline.core.plugin.RedemptivePlugin;
import tech.rayline.core.util.SoundUtil;

import java.util.*;

/**
 * This class represents an InventoryGUI which can be opened for players
 *
 * @version 2.0
 */
public class InventoryGUI {
    /**
     * A collection of all the players who currently have the inventory GUI open
     */
    private final Set<UUID> observers = new HashSet<>();
    /**
     * All the current button mappings for different slots
     */
    private final Map<Integer, InventoryGUIButton> buttons = new HashMap<>();
    /**
     * The slots which have been updated by our mutator methods, but have yet to be put in the linked bukkit inventory
     */
    private final Set<Integer> touchedSlots = new HashSet<>();
    /**
     * The plugin which created this inventory GUI
     */
    private final RedemptivePlugin plugin;
    /**
     * The linked bukkit inventory
     */
    private final Inventory bukkitInventory;
    /**
     * The event handling subscription- this can be unsubscribed from to cause this class to fall completely out of scope
     * @see #invalidate()
     */
    private final Subscription mainSubscription;

    /**
     * Allows you to create an inventory GUI of a certain type
     * @param plugin The plugin which this GUI is a member of
     * @param type The type of inventory to use
     * @param title The title of the GUI
     */
    public InventoryGUI(RedemptivePlugin plugin, InventoryType type, String title) {
        this.plugin = plugin;
        if (type == InventoryType.CHEST)
            throw new IllegalArgumentException("You must use the constructor accepting an integer, not InventoryType, if you wish to create a standard chest inventory!");

        bukkitInventory = Bukkit.createInventory(null, type, title);
        mainSubscription = beginObserving();
    }

    /**
     * Allows you to create a chest inventory GUI of a certain size
     * @param plugin The plugin which this GUI is a member of
     * @param size The size of the inventory
     * @param title The title of the GUI
     */
    public InventoryGUI(RedemptivePlugin plugin, int size, String title) {
        this.plugin = plugin;
        bukkitInventory = Bukkit.createInventory(null, size, title);
        mainSubscription = beginObserving();
    }

    //creates and returns the subscription for the main events
    private Subscription beginObserving() {
        //using a composite subscription because we have two entirely separate subscriptions to two different events. We want to still represent this as a single subscription, however.
        final CompositeSubscription subscription = new CompositeSubscription();
        //noinspection SuspiciousMethodCalls
        subscription.add(
                //this is the main subscription that we use to actually catch clicks
                plugin.observeEvent(InventoryClickEvent.class)
                .filter(new Func1<InventoryClickEvent, Boolean>() {
                    @Override
                    public Boolean call(InventoryClickEvent event) {
                        //noinspection SuspiciousMethodCalls
                        return event.getInventory().getTitle().equals(bukkitInventory.getTitle()) && observers.contains(event.getWhoClicked().getUniqueId());
                    }
                })
                .subscribe(new Action1<InventoryClickEvent>() {
                    @Override
                    public void call(InventoryClickEvent event) {
                        event.setCancelled(true);

                        try {
                            InventoryGUIButton buttonAt = getButtonAt(event.getRawSlot());
                            if (buttonAt == null)
                                return;

                            Player whoClicked = (Player) event.getWhoClicked();
                            try {
                                buttonAt.onPlayerClick(whoClicked, ClickAction.from(event.getClick()));
                                whoClicked.updateInventory();
                            } catch (EmptyHandlerException e) {
                                SoundUtil.playTo(whoClicked, Sound.BLOCK_NOTE_PLING);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));

        return subscription;
    }

    public Observable<InventoryGUIAction> observeSlot(final ItemStack buttonStack, final Integer slot) {
        return Observable.create(new Observable.OnSubscribe<InventoryGUIAction>() {
            @Override
            public void call(final Subscriber<? super InventoryGUIAction> subscriber) {
                InventoryGUI.this.setButton(new SimpleInventoryGUIButton(buttonStack, new Action1<InventoryGUIAction>() {
                    @Override
                    public void call(InventoryGUIAction action) {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onNext(action);
                    }
                }) {
                    @Override
                    protected void onRemove() {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onCompleted();
                    }

                    @Override
                    protected void onAdd() {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onStart();
                    }
                }, slot);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        InventoryGUI.this.clearButton(slot);
                    }
                }));
            }
        });
    }

    /**
     * This will force the inventory to fall out of scope by clearing and un-subscribing various things
     */
    public void invalidate() {
        mainSubscription.unsubscribe();
        for (UUID observer : observers)
            Bukkit.getPlayer(observer).closeInventory();
        observers.clear();
        touchedSlots.clear();
        for (Integer integer : buttons.keySet())
            clearButton(integer);
    }

    public void addButton(InventoryGUIButton button) {
        setButton(button, getNextSlot());
    }

    public ImmutableList<InventoryGUIButton> getButtons() {
        return ImmutableList.copyOf(buttons.values());
    }

    /**
     * Changes the button at a location
     * @param button The button you wish to now be at that location
     * @param slot The slot location
     */
    public void setButton(InventoryGUIButton button, Integer slot) {
        InventoryGUIButton buttonAt = getButtonAt(slot);
        if (buttonAt == null || !buttonAt.equals(button))
            button.onAdd();
        clearButton(slot);

        buttons.put(slot, button);
        markForUpdate(slot);
    }

    /**
     * Removes a button from the inventory GUI
     * @param slot The slot of the button
     */
    public void clearButton(Integer slot) {
        InventoryGUIButton remove = buttons.remove(slot);
        if (remove != null)
            remove.onRemove();
        markForUpdate(slot);
    }

    /**
     * Check if a button is in a slot
     * @param slot The slot to check
     * @return the presence of a button in that slot
     */
    public boolean hasButton(Integer slot) {
        return buttons.containsKey(slot);
    }

    /**
     * Gets you the current inventory button at a location
     * @param slot The location to get the button from
     * @return The button at that location, or null if there is none
     */
    public InventoryGUIButton getButtonAt(Integer slot) {
        return buttons.get(slot);
    }

    /**
     * Forces all currently staged modifications to the inventory to appear for all clients. This must be called if you call any of the following methods:
     *
     * <ol>
     *     <li>{@link #setButton(InventoryGUIButton, Integer)}</li>
     *     <li>{@link #clearButton(Integer)}</li>
     * </ol>
     */
    public void updateInventory() {
        for (Integer touchedSlot : touchedSlots) {
            InventoryGUIButton buttonAt = getButtonAt(touchedSlot);
            boolean b = buttonAt == null;
            if (!b && buttonAt.getCurrentRepresentation() == null)
                throw new IllegalStateException("Your inventory button " + buttonAt.toString() + " has failed to provide an item during the update cycle!");
            ItemStack itemStack = b ? null : buttonAt.getCurrentRepresentation();
            bukkitInventory.setItem(touchedSlot, itemStack);
        }

        for (UUID observer : observers)
            Bukkit.getPlayer(observer).updateInventory();
        touchedSlots.clear();
    }

    /**
     * Opens the inventory for the player
     * @param player The player who you want to show this inventory to
     */
    public void openFor(final Player player) {
        if (mainSubscription.isUnsubscribed())
            throw new IllegalStateException("You cannot use this inventory anymore! You have invalidated it!");

        observers.add(player.getUniqueId());
        //listens to the player quit event and the inventory close event
        Observable.merge(
                plugin.observeEvent(PlayerQuitEvent.class).map(new Func1<PlayerQuitEvent, Player>() {
                    @Override
                    public Player call(PlayerQuitEvent playerQuitEvent) {
                        return playerQuitEvent.getPlayer();
                    }
                }),
                plugin.observeEvent(InventoryCloseEvent.class).map(new Func1<InventoryCloseEvent, Player>() {
                    @Override
                    public Player call(InventoryCloseEvent inventoryCloseEvent) {
                        return (Player) inventoryCloseEvent.getPlayer();
                    }
                })
                //with both- filter out where the player is not the one we're looking for
                .filter(new Func1<Player, Boolean>() {
                    @Override
                    public Boolean call(Player pl) {
                        return pl.equals(player);
                    }
                })
                //only take one, and only while the player still has the inventory open
                .takeWhile(new Func1<Player, Boolean>() {
                    @Override
                    public Boolean call(Player pl) {
                        return InventoryGUI.this.isOpenFor(player);
                    }
                }))
                .take(1)
                //notify us that the inventory has been closed!
                .subscribe(new Action1<Player>() {
                    @Override
                    public void call(Player player) {
                        inventoryClosed(player);
                    }
                });

        //open the inventory
        player.openInventory(bukkitInventory);
    }

    public boolean isOpenFor(Player player) {
        return observers.contains(player.getUniqueId());
    }

    /**
     * Closes the inventory for the player- this is immediate.
     *
     * This call will simlpy do nothing if the player does not currently have our inventory open (at least, in our eyes)
     * @param player The player who you want to close the inventory for
     */
    public void closeFor(Player player) {
        if (!observers.contains(player.getUniqueId())) return;
        player.closeInventory();
        inventoryClosed(player);
    }

    //removes players from the observable set now that they've closed the inventory
    protected void inventoryClosed(Player player) {
        observers.remove(player.getUniqueId());
    }

    //used to mark a slot for update
    protected void markForUpdate(Integer slot) {
        touchedSlots.add(slot);
    }

    public int getSlotFor(InventoryGUIButton button) {
        for (Map.Entry<Integer, InventoryGUIButton> entry : buttons.entrySet()) {
            if (entry.getValue().equals(button))
                return entry.getKey();
        }

        throw new NoSuchElementException("Could not find that button!");
    }

    protected void markForUpdate(InventoryGUIButton button) {
        markForUpdate(getSlotFor(button));
    }

    public void removeButton(InventoryGUIButton button) {
        removeButton(getSlotFor(button));
    }

    public void removeButton(int slot) {
        clearButton(slot);
    }

    public Integer getNextSlot() {
        for (int i = 0; i < bukkitInventory.getSize(); i++) {
            if (getButtonAt(i) == null)
                return i;
        }
        throw new ArrayIndexOutOfBoundsException("There are no more slots in the inventory!");
    }
}

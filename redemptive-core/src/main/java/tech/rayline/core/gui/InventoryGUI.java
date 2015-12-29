package tech.rayline.core.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import tech.rayline.core.command.EmptyHandlerException;
import tech.rayline.core.plugin.RedemptivePlugin;
import tech.rayline.core.util.SoundUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an InventoryGUI which can be opened for players
 *
 * @version 2.0
 */
public final class InventoryGUI {
    /**
     * A collection of all the players who currently have the inventory GUI open
     */
    private final Set<Player> observers = new HashSet<>();
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
        CompositeSubscription subscription = new CompositeSubscription();
        //noinspection SuspiciousMethodCalls
        subscription.add(
                //this is the main subscription that we use to actually catch clicks
                plugin.observeEvent(InventoryClickEvent.class)
                .filter(event -> event.getInventory().equals(bukkitInventory) && observers.contains(event.getWhoClicked()))
                .subscribe(event -> {
                    event.setCancelled(true);
                    try {
                        InventoryGUIButton buttonAt = getButtonAt(event.getSlot());
                        if (buttonAt == null)
                            return;

                        Player whoClicked = (Player) event.getWhoClicked();
                        try {
                            buttonAt.onPlayerClick(whoClicked, ClickAction.from(event.getClick()));
                        } catch (EmptyHandlerException e) {
                            SoundUtil.playTo(whoClicked, Sound.NOTE_PLING);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

        subscription.add(
                //this prevents people from moving items in and out of the inventory GUI
                plugin.observeEvent(InventoryMoveItemEvent.class)
                .filter(event -> event.getSource().equals(bukkitInventory) || event.getDestination().equals(bukkitInventory))
                .subscribe(event -> event.setCancelled(true))
        );

        return subscription;
    }

    public Observable<InventoryGUIAction> observeSlot(ItemStack buttonStack, Integer slot) {
        return Observable.create((Observable.OnSubscribe<InventoryGUIAction>) subscriber -> {
            setButton(new SimpleInventoryGUIButton(buttonStack, action -> {
                if (!subscriber.isUnsubscribed())
                    subscriber.onNext(action);
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

            subscriber.add(Subscriptions.create(() -> clearButton(slot)));
        });
    }



    /**
     * This will force the inventory to fall out of scope by clearing and un-subscribing various things
     */
    public void invalidate() {
        mainSubscription.unsubscribe();
        observers.forEach(Player::closeInventory);
        observers.clear();
        touchedSlots.clear();
        buttons.clear();
    }

    /**
     * Changes the button at a location
     * @param button The button you wish to now be at that location
     * @param slot The slot location
     */
    public void setButton(InventoryGUIButton button, Integer slot) {
        if (!getButtonAt(slot).equals(button))
            button.onAdd();

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

        observers.forEach(Player::updateInventory);
        touchedSlots.clear();
    }

    /**
     * Opens the inventory for the player
     * @param player The player who you want to show this inventory to
     */
    public void openFor(Player player) {
        if (mainSubscription.isUnsubscribed())
            throw new IllegalStateException("You cannot use this inventory anymore! You have invalidated it!");

        observers.add(player);
        //listens to the player quit event and the inventory close event
        Observable.merge(
                plugin.observeEvent(PlayerQuitEvent.class).map(PlayerEvent::getPlayer),
                plugin.observeEvent(InventoryCloseEvent.class).map(InventoryCloseEvent::getPlayer).cast(Player.class))
                //with both- filter out where the player is not the one we're looking for
                .filter(pl -> pl.equals(player))
                //only take one, and only while the player still has the inventory open
                .takeWhile(pl -> isOpenFor(player))
                .take(1)
                //notify us that the inventory has been closed!
                .subscribe(this::inventoryClosed);

        //open the inventory
        player.openInventory(bukkitInventory);
    }

    public boolean isOpenFor(Player player) {
        return observers.contains(player);
    }

    /**
     * Closes the inventory for the player- this is immediate.
     *
     * This call will simlpy do nothing if the player does not currently have our inventory open (at least, in our eyes)
     * @param player The player who you want to close the inventory for
     */
    public void closeFor(Player player) {
        if (!observers.contains(player)) return;
        player.closeInventory();
        inventoryClosed(player);
    }

    //removes players from the observable set now that they've closed the inventory
    private void inventoryClosed(Player player) {
        observers.remove(player);
    }

    //used to mark a slot for update
    private void markForUpdate(Integer slot) {
        touchedSlots.add(slot);
    }
}

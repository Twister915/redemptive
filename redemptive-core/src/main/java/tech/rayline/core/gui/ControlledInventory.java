package tech.rayline.core.gui;

import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
@Data
public abstract class ControlledInventory implements Listener {
    private final Map<Integer, ControlledInventoryButton> buttons = new HashMap<Integer, ControlledInventoryButton>();
    private final Set<Player> players = new HashSet<Player>();

    public ControlledInventory() {
        this(true);
    }

    public ControlledInventory(boolean reload) {
        if (reload) reload();
    }

    protected abstract ControlledInventoryButton getNewButtonAt(Integer slot);

//    @Override
//    public final void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {}
//
//    @Override
//    public final void onPlayerDisconnect(CPlayer player) {
//        players.remove(player);
//    }

    public final void reload() {
        buttons.clear();
        for (int i = 0; i < 36; i++) {
            ControlledInventoryButton newButtonAt = getNewButtonAt(i);
            if (newButtonAt == null) continue;
            buttons.put(i, newButtonAt);
        }
        for (Player player : players) {
            updateForPlayer(player);
        }
    }

    public final void updateItems() {
        for (Player player : players) {
            updateForPlayer(player);
        }
    }

    public final void setActive(Player player) {
        players.add(player);
        updateForPlayer(player);
    }

    public final void remove(Player player) {
        if (!players.contains(player)) return;
        clearForPlayer(player);
        players.remove(player);
    }

    protected void updateForPlayer(Player player) {
        for (Map.Entry<Integer, ControlledInventoryButton> entry : buttons.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().getStack(player));
        }
        player.updateInventory();
    }

    private void clearForPlayer(Player player) {
        for (Integer integer : buttons.keySet()) {
            player.getInventory().setItem(integer, null);
        }
        player.updateInventory();
    }

    @EventHandler
    public final void onPlayerQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public final void onPlayerInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player onlinePlayer = (Player) event.getWhoClicked();
        if (!players.contains(onlinePlayer)) return;
        if (buttons.keySet().contains(event.getSlot())
                && players.contains(onlinePlayer)
                && event.getClickedInventory().equals(onlinePlayer.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public final void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getAction() == Action.PHYSICAL) return;
        ControlledInventoryButton controlledInventoryButton = buttons.get(event.getPlayer().getInventory().getHeldItemSlot());
        if (controlledInventoryButton == null) return;
        Player onlinePlayer = event.getPlayer();
        if (!players.contains(onlinePlayer)) return;
//        try {
//            onlinePlayer.getCooldownManager().testCooldown((controlledInventoryButton.hashCode() + "_inv"), 1L, TimeUnit.SECONDS);
//        } catch (CooldownUnexpiredException e) {
//            if (e.getTimeRemaining() > 800)
//                return;
//            else //TODO make a sound and send a message.
//                return;
//        }
        controlledInventoryButton.onUse(onlinePlayer);
        updateForPlayer(onlinePlayer);
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW)
    public final void onPlayerDrop(PlayerDropItemEvent event) {
        Player onlinePlayer = event.getPlayer();
        if (!players.contains(onlinePlayer)) return;
        if (buttons.get(onlinePlayer.getInventory().getHeldItemSlot()) != null) {
            event.setCancelled(false);
            event.getItemDrop().remove();
            updateForPlayer(onlinePlayer);
//            try {
//                onlinePlayer.getCooldownManager().testCooldown(hashCode() + "_inv_drop", 500L, TimeUnit.MILLISECONDS, false);
//            } catch (CooldownUnexpiredException e) {
//                onlinePlayer.kickPlayer(ChatColor.RED + "Spamming inventory drops (more than 1 per second)");
//            }
        }
    }


    protected final Set<Map.Entry<Integer, ControlledInventoryButton>> getButtons() {
        return buttons.entrySet();
    }
}

package tech.rayline.core.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@Getter
@ToString(of = {"from", "origin", "restored"})
@EqualsAndHashCode(of = {"origin", "from"})
public final class PlayerState {
    private final UUID origin;
    private final ItemStack[] inventory, armour;
    private final GameMode mode;
    private final PotionEffect[] effects;
    private final double health, food, walkSpeed, flySpeed;
    private final boolean allowFlight;
    private final Location from;

    private transient boolean restored;

    public PlayerState(Player player) {
        origin = player.getUniqueId();
        player.closeInventory();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents(), armourContents = inventory.getArmorContents();
        this.inventory = Arrays.copyOf(contents, contents.length);
        armour = Arrays.copyOf(armourContents, armourContents.length);
        mode = player.getGameMode();
        Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
        effects = activePotionEffects.toArray(new PotionEffect[activePotionEffects.size()]);
        health = player.getHealth();
        food = player.getFoodLevel();
        walkSpeed = player.getWalkSpeed();
        flySpeed = player.getFlySpeed();
        allowFlight = player.getAllowFlight();
        from = player.getLocation();

        //clear
        reset(true);
    }

    public void reset(boolean flying) {
        OfflinePlayer offlinePlayer = getPlayer();
        if (!offlinePlayer.isOnline())
            throw new IllegalStateException("The player is not currently online!");
        Player player = offlinePlayer.getPlayer();

        PlayerInventory inventory = player.getInventory();

        inventory.setArmorContents(new ItemStack[4]);
        inventory.clear();
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setWalkSpeed(1f);
        player.setFlySpeed(1f);
        player.setAllowFlight(flying);
        if (flying)
            player.setFlying(true);
        player.setFallDistance(0f);
        player.setGameMode(GameMode.SPECTATOR);
        restored = false;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(origin);
    }

    public void restore() {
        restore(false);
    }

    public void restore(boolean force) {
        if (restored && !force)
            throw new IllegalStateException("This player state has already been restored!");

        reset(false);

        OfflinePlayer offlinePlayer = getPlayer();
        if (!offlinePlayer.isOnline())
            throw new IllegalStateException("The player is not currently online!");
        Player player = offlinePlayer.getPlayer();

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(armour);
        inventory.setContents(this.inventory);
        player.updateInventory();
        player.setGameMode(mode);
        player.setFlySpeed((float) flySpeed);
        player.setWalkSpeed((float) walkSpeed);
        player.setAllowFlight(allowFlight);
        if (allowFlight) player.setFlying(true);
        player.teleport(from);
        for (PotionEffect effect : effects) player.addPotionEffect(effect);
        player.setHealth(health);
        player.setFoodLevel((int) food);

        restored = true;
    }
}

package tech.rayline.core.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerUtil {

    public static void resetPlayer(Player player) {
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.resetPlayerTime();
        player.resetPlayerWeather();
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setExp(0);
        player.setLevel(0);
        if (player.getAllowFlight()) player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
    }
}

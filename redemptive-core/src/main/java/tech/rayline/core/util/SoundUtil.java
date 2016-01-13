package tech.rayline.core.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {
    public static void playTo(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 20f, 0f);
    }
}

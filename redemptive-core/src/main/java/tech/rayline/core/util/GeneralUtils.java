package tech.rayline.core.util;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Random;

public final class GeneralUtils {
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return false;
            for (File file1 : files) {
                if (!delete(file1)) return false;
            }
        }
        return file.delete();
    }

    public static void copy(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
        if (source.isDirectory()) {
            for (String s : source.list()) {
                copy(new File(source, s), new File(dest, s));
            }
        }
    }

    public static <T> boolean arrayContains(T[] ts, T t) {
        for (T t1 : ts) {
            if ((t1 == null || t == null) && t != t1) continue;
            if (t1 == t || t1.equals(t)) return true;
        }
        return false;
    }

    public static <T> T[] grow(Class<T> type, T[] array) {
        T[] ts = (T[]) Array.newInstance(type, array.length << 1);
        System.arraycopy(array, 0, ts, 0, array.length);
        return ts;
    }

    public static <T> T[] trim(Class<T> type, T[] array) {
        int len = 0;
        while (array[len] != null && len < array.length) len++;
        T[] ts = (T[]) Array.newInstance(type, len);
        System.arraycopy(array, 0, ts, 0, len);
        return ts;
    }

    public static void randomlySpawnFireworks(RedemptivePlugin plugin, final World world, final Point center, final Point offset, int magnitude) {
        final Random random = new Random();
        for (int i = 0; i < random.nextInt(magnitude) + 2; i++)
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    Location fireworkOrig = new Point(
                            center.getX() + (random.nextGaussian() * offset.getX()),
                            center.getY() + (random.nextGaussian() * offset.getY()),
                            center.getZ() + (random.nextGaussian() * offset.getZ()),
                            0F, 0F).in(world);
                    Firework firework = (Firework) world.spawnEntity(fireworkOrig, EntityType.FIREWORK);
                    FireworkMeta fireworkMeta = firework.getFireworkMeta();
                    fireworkMeta.setPower(1);
                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.with(getRandom(FireworkEffect.Type.values()));
                    for (int m = 0; m < random.nextInt(3) + 2; m++)
                        builder.withColor(getRandom(DyeColor.values()).getColor());
                    if (Math.random() < 0.5) builder.withFlicker();
                    if (Math.random() < 0.5) builder.withTrail();
                    fireworkMeta.addEffect(builder.build());
                    firework.setFireworkMeta(fireworkMeta);
                }
            }, i * 3);
    }

    private static <T> T getRandom(T[] ts) {
        return ts[((int) (Math.random() * ts.length))];
    }

    public static int getFreeSlots(Player player) {
        int freeSlots = 0;
        for (int i = 0; i < 36; i++) freeSlots += (player.getInventory().getItem(i) == null ? 1 : 0);

        return freeSlots;
    }

    public static String formatSeconds(Integer seconds) {
        StringBuilder builder = new StringBuilder();
        int ofNext = seconds;
        for (TimeUnit unit : TimeUnit.values()) {
            int ofUnit;
            if (unit.perNext != -1) {
                ofUnit = ofNext % unit.perNext;
                ofNext = Math.floorDiv(ofNext, unit.perNext);
            }
            else {
                ofUnit = ofNext;
                ofNext = 0;
            }
            builder.insert(0, unit.shortName).insert(0, String.format("%02d", ofUnit));
            if (ofNext == 0) break;
        }
        return builder.toString();
    }

    private enum TimeUnit {
        SECONDS(60, 's'),
        MINUTES(60, 'm'),
        HOURS(24, 'h'),
        DAYS('d');

        private final int perNext;
        private final char shortName;

        TimeUnit(int i, char h) {
            perNext = i;
            shortName = h;
        }

        TimeUnit(char d) {
            perNext = -1;
            shortName = d;
        }
    }
}

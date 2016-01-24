package tech.rayline.core.util;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

@Data
public final class Point {
    private final double x, y, z;
    private final float pitch, yaw;

    public Location in(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double distanceSquared(Point point) {
        return distanceSquared(point.getX(), point.getY(), point.getZ());
    }

    public double distanceSquared(Location location) {
        return distanceSquared(location.getX(), location.getY(), location.getZ());
    }

    public double distanceSquared(double x, double y, double z) {
        return distanceSquared(x, z) + Math.pow(this.y - y, 2);
    }

    public double distanceSquared(double x, double z) {
        return Math.pow(this.x - x, 2) + Math.pow(this.z - z, 2);
    }

    public double distance(double x, double y, double z) {
        return Math.sqrt(distanceSquared(x, y, z));
    }

    public double distance(double x, double z) {
        return Math.sqrt(distanceSquared(x, z));
    }

    public double distance(Point point) {
        return Math.sqrt(distanceSquared(point));
    }

    public double distance(Location location) {
        return Math.sqrt(distanceSquared(location));
    }

    public static Point of(Location location) {
        return new Point(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public static Point of(Block block) {
        return new Point(block.getX(), block.getY(), block.getZ(), 0f, 0f);
    }

    public static Point of(Entity entity) {
        return of(entity.getLocation());
    }

    public Point add(double x, double y, double z) {
        return new Point(this.x + x, this.y + y, this.z + z, pitch, yaw);
    }

    public Point subtract(double x, double y, double z) {
        return add(-x, -y, -z);
    }
}

package tech.rayline.core.util;

import lombok.Data;

@Data
public final class Region {
    private final Point min, max;

    private final double width, height, depth;

    public Region(Point a, Point b) {
        min = new Point(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), Math.min(a.getPitch(), b.getPitch()), Math.min(a.getYaw(), b.getYaw()));
        max = new Point(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()), Math.max(a.getPitch(), b.getPitch()), Math.max(a.getYaw(), b.getYaw()));

        width = max.getX() - min.getX();
        height = max.getY() - min.getX();
        depth = max.getZ() - min.getZ();
    }

    public boolean inRegion(Point point) {
        return inRegion(point.getX(), point.getY(), point.getZ());
    }

    public boolean inRegion(double x, double y, double z) {
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }
}

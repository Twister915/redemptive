package tech.rayline.core.util;

import lombok.Data;

@Data
public final class Region {
    private Point min, max;

    public Region(Point a, Point b) {
        setMinMax(a, b);
    }

    public void setMin(Point point) {
        setMinMax(max, point);
    }

    public void setMax(Point point) {
        setMinMax(min, point);
    }

    public void setMinMax(Point a, Point b) {
        min = new Point(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), Math.min(a.getPitch(), b.getPitch()), Math.min(a.getYaw(), b.getYaw()));
        max = new Point(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()), Math.max(a.getPitch(), b.getPitch()), Math.max(a.getYaw(), b.getYaw()));
    }

    public boolean inRegion(Point point) {
        return point.getX() >= min.getX() && point.getX() <= max.getX()
                && point.getY() >= min.getY() && point.getY() <= max.getY()
                && point.getZ() >= min.getZ() && point.getZ() <= max.getZ();
    }
}

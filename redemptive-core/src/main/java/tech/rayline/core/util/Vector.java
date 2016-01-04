package tech.rayline.core.util;

import lombok.Data;

@Data
public final class Vector {
    private final Point origin;
    private final double theta, mag;
}

package tech.rayline.core.gui;

import org.bukkit.event.inventory.ClickType;

public enum ClickAction {
    RIGHT_CLICK,
    LEFT_CLICK;

    public static ClickAction from(ClickType action) {
        switch (action) {
            case RIGHT:
            case SHIFT_RIGHT:
                return ClickAction.RIGHT_CLICK;
            default:
                return ClickAction.LEFT_CLICK;
        }
    }
}

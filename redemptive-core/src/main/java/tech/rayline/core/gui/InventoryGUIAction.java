package tech.rayline.core.gui;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class InventoryGUIAction {
    private final Player player;
    private final ClickAction action;
}

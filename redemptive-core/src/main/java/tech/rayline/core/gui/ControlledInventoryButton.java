package tech.rayline.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ControlledInventoryButton {
    protected void onUse(Player player) {}
    protected abstract ItemStack getStack(Player player);
}

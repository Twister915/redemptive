package tech.rayline.core.gui;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tech.rayline.core.command.EmptyHandlerException;

@Getter
public abstract class InventoryGUIButton {
    private ItemStack currentRepresentation;

    public InventoryGUIButton() {}
    public InventoryGUIButton(ItemStack stack) {
        this.currentRepresentation = stack;
    }

    protected void onPlayerClick(Player player, ClickAction action) throws EmptyHandlerException { throw new EmptyHandlerException(); }
    protected void onRemove() {}
    protected void onAdd() {}
    protected void onUpdate() {}
}

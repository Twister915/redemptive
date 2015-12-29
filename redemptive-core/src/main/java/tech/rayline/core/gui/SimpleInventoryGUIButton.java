package tech.rayline.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rx.functions.Action1;
import tech.rayline.core.command.EmptyHandlerException;

public class SimpleInventoryGUIButton extends InventoryGUIButton {
    private final Action1<InventoryGUIAction> action;

    public SimpleInventoryGUIButton(ItemStack stack, Action1<InventoryGUIAction> action) {
        super(stack);
        this.action = action;
    }

    @Override
    protected void onPlayerClick(Player player, ClickAction action) throws EmptyHandlerException {
        try {
            this.action.call(new InventoryGUIAction(player, action));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package tech.rayline.core.command;

import org.bukkit.ChatColor;

public final class PermissionException extends CommandException implements FriendlyException {
    public PermissionException(String message) {
        super(message);
    }

    @Override
    public String getFriendlyMessage(RDCommand command) {
        return ChatColor.RED + "You do not have permission for this!";
    }
}

package tech.rayline.core.command;

import org.bukkit.ChatColor;

public final class NormalCommandException extends CommandException implements FriendlyException {
    public NormalCommandException(String message) {
        super(message);
    }

    @Override
    public String getFriendlyMessage(RDCommand command) {
        return ChatColor.RED + getMessage();
    }
}

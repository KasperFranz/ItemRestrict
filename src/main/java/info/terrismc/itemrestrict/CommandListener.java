package info.terrismc.itemrestrict;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {

    private final ConfigStore cStore;

    public CommandListener(ItemRestrict plugin) {
        this.cStore = plugin.cStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "reload":
                if (args.length == 1) {
                    cStore.reloadConfig();
                    sender.sendMessage("ItemRestrict Config Reloaded");
                    return true;
                }

            case "hand":
                if (args.length == 1) {
                    cStore.getInformationInHand(sender);
                    return true;
                }
            case "ban":
                if (args.length == 3) {
                    cStore.addBan(sender, getActionType(args[1]), args[2]);
                    return true;
                }
            case "unban":
                if (args.length == 3) {
                    cStore.removeBan(sender, getActionType(args[1]), args[2]);
                    return true;
                }
        }
        return false;
    }

    private ActionType getActionType(String actionTypeString) {
    	for(final ActionType type : ActionType.values())
    	{
    		if(type.name().compareToIgnoreCase(actionTypeString) == 0)
    		{
    			return type;
    		}
    	}
    	return null;
    }
}

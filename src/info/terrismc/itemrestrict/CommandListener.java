package info.terrismc.itemrestrict;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
	private ConfigStore cStore;
	
	public CommandListener( ItemRestrict plugin ) {
		this.cStore = plugin.cStore;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if( args.length == 0 )
			return false;
		switch( args[0] ) {
		case "reload":
			if( args.length == 1 ) {
				cStore.reloadPlugin();
				return true;	
			}
		}
		return false;
	}

}

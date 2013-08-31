package info.terrismc.TekkitCustomizer;

import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ConfigStore {
	TekkitCustomizer plugin;
	
	// Cache config values
	private List<String> worldList;
	private List<String> usageBans;
	private List<String> ownershipBans;
	private List<String> placementBans;
	private List<String> worldBans;
	private List<String> craftingBans;
	
	public ConfigStore( TekkitCustomizer plugin ) {
		this.plugin = plugin;
		
		// Force reload plugin
		reloadPlugin();
	}
	
	private void reloadPlugin() {
		plugin.saveDefaultConfig();
		worldList = plugin.getConfig().getStringList( "Worlds" );
		usageBans = plugin.getConfig().getStringList( "Bans.Usage" );
		ownershipBans = plugin.getConfig().getStringList( "Bans.Ownership" );
		placementBans = plugin.getConfig().getStringList( "Bans.Placement" );
		worldBans = plugin.getConfig().getStringList( "Bans.World" );
		craftingBans = plugin.getConfig().getStringList( "Bans.Crafting" );
	}

	public boolean isEnabledWorld( World world ) {
		return worldList.contains( world.getName() );
	}
	
	public boolean isBanned( Block block, ActionType actionType ) {
		return isBanned( getConfigString( block ), actionType );
	}
	
	public boolean isBanned( ItemStack item, ActionType actionType ) {
		return isBanned( getConfigString( item ), actionType );
	}
	
	private boolean isBanned( String configString, ActionType actionType ) {
		// Select proper HashMap to pull banned response for
		switch( actionType ) {
		case Usage:
			return usageBans.contains( configString );
		case Ownership:
			return ownershipBans.contains( configString );
		case Placement:
			return placementBans.contains( configString );
		case World:
			return worldBans.contains( configString );
		case Crafting:
			return craftingBans.contains( configString );
		default:
			// Should never reach here if all enum cases covered
			TekkitCustomizer.logger.warning( "Unknown ActionType detected: " + actionType.toString() );
			return false;
		}
	}
	
	public String getLabel( Block block ) {
		return plugin.getConfig().getString( "Messages.Reasons." + getConfigString( block ) );
	}
	
	public String getLabel( ItemStack item ) {
		return plugin.getConfig().getString( "Messages.Reasons." + getConfigString( item ) );
	}
	
	public String getReason( Block block ) {
		return plugin.getConfig().getString( "Messages.Reasons." + getConfigString( block ) );
	}
	
	public String getReason( ItemStack item ) {
		return plugin.getConfig().getString( "Messages.Reasons." + getConfigString( item ) );
	}
	
	private String getConfigString( Block block ) {
		// Config version string of block and data value 
		return "" + block.getTypeId() + "-" + block.getData();
	}
	
	private String getConfigString( ItemStack item ) {
		// Config version string of item and data value
		MaterialData matData = item.getData();
		return "" + matData.getItemTypeId() + "-" + matData.getData();
	}

	public void getTest() {
		// TODO Auto-generated method stub
		
	}
}

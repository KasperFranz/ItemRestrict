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
		boolean banned = isBanned( getConfigString( block ), actionType );
		if( !banned )
			banned = isBanned( getConfigStringParent( block ), actionType );
		return banned; 
	}
	
	public boolean isBanned( ItemStack item, ActionType actionType ) {
		boolean banned = isBanned( getConfigString( item ), actionType );
		if( !banned )
			banned = isBanned( getConfigStringParent( item ), actionType );
		return banned;
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
	
	public boolean isBannable( ItemStack item, ActionType actionType, World world ) {
		// Check null
		if( item == null )
			return false;
		
		// Check world
		if( !isEnabledWorld( world ) )
			return false;
		
		// Check banned
		return isBanned( item, actionType );
	}
	
	public boolean isBannable( Block block, ActionType actionType ) {
		// Check world
		if( !isEnabledWorld( block.getWorld() ) ) return false;
		
		// Check banned
		return isBanned( block, actionType );
	}
	
	public String getLabel( Block block ) {
		String label = plugin.getConfig().getString( "Messages.label." + getConfigString( block ) );
		if( label != null )
			return label;
		label = plugin.getConfig().getString( "Messages.label." + getConfigStringParent( block ) );
		if( label != null )
			return label;
		return block.getType().name() + " (" + getConfigString( block ) + ")";
	}
	
	public String getLabel( ItemStack item ) {
		String label = plugin.getConfig().getString( "Messages.label." + getConfigString( item ) );
		if( label != null )
			return label;
		label = plugin.getConfig().getString( "Messages.label." + getConfigStringParent( item ) );
		if( label != null )
			return label;
		return item.getType().name() + " (" + getConfigString( item ) + ")";
	}
	
	public String getReason( Block block ) {
		String reason = plugin.getConfig().getString( "Messages.reasons." + getConfigString( block ) );
		if( reason != null )
			return reason;
		reason = plugin.getConfig().getString( "Messages.reasons." + getConfigStringParent( block ) );
		if( reason != null )
			return reason;
		return "Ask your server administrator.";
	}
	
	public String getReason( ItemStack item ) {
		String reason = plugin.getConfig().getString( "Messages.reasons." + getConfigString( item ) );
		if( reason != null )
			return reason;
		reason = plugin.getConfig().getString( "Messages.reasons." + getConfigStringParent( item ) );
		if( reason != null )
			return reason;
		return "Ask your server administrator.";
	}
	
	private String getConfigString( Block block ) {
		// Config version string of block id and data value 
		return "" + block.getTypeId() + "-" + block.getData();
	}
	
	private String getConfigStringParent( Block block ) {
		// Config version string of block id 
		return "" + block.getTypeId();
	}
	
	private String getConfigString( ItemStack item ) {
		// Config version string of item id and data value
		MaterialData matData = item.getData();
		return "" + matData.getItemTypeId() + "-" + matData.getData();
	}
	
	private String getConfigStringParent( ItemStack item ) {
		// Config version string of item id and data value
		return "" + item.getTypeId();
	}
}

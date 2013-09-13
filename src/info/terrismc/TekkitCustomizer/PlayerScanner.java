package info.terrismc.TekkitCustomizer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerScanner {
	private ConfigStore cStore;
	
	public PlayerScanner( TekkitCustomizer plugin ) {
		this.cStore = plugin.cStore;
	}
	
	public void scanInventory( Player player ) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
		for( int i = 0; i < items.length; i++ )
			if( cStore.isBanned( items[i] , ActionType.Ownership ) )
				inventory.setItem( i, null );
	}
}
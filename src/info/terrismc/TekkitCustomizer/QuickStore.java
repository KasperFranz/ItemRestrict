package info.terrismc.TekkitCustomizer;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class QuickStore {
	private TekkitCustomizer plugin;
	private ConfigStore cStore;
	
	public QuickStore( TekkitCustomizer plugin ) {
		this.plugin = plugin;
		this.cStore = plugin.cStore;
	}
	
	public void scanInventory( final Player player ) {
		TekkitCustomizer.server.getScheduler().runTaskAsynchronously( plugin, new Runnable() {
			public void run() {
				World world = player.getWorld();
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				for( int i = 0; i < items.length; i++ )
					if( cStore.isBannable( items[i] , ActionType.Ownership, world ) )
						inventory.setItem( i, null );
			}
		});
	}
}

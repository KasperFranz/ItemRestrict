package info.terrismc.TekkitCustomizer;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

public class QuickStore {
	final private TekkitCustomizer plugin;
	private ConfigStore cStore;
	
	public QuickStore( TekkitCustomizer plugin ) {
		this.plugin = plugin;
		this.cStore = plugin.cStore;
	}
	
	public void scanChunk( final Chunk chunk ) {
		if( cStore.getBanListSize( ActionType.World ) == 0 ) return;
		
		final BukkitScheduler scheduler = TekkitCustomizer.server.getScheduler();
		scheduler.runTaskAsynchronously( plugin, new Runnable() {
			public void run() {
				final int yMax = chunk.getWorld().getMaxHeight();
				Block block;
				for( int x = 0; x < 16; x++ ) {
					for( int z = 0; z < 16; z++ ) {
						for( int y = 0; y < yMax; y++ ) {
							block = chunk.getBlock( x, y, z );
							if( cStore.isBannable( block, ActionType.World ) ) {
								TekkitCustomizer.logger.warning("Found bannable block");
								block.setType( Material.AIR );
							}
						}
					}
				}
			}
		});
	}
	
	public void scanInventory( final Player player ) {
		if( cStore.getBanListSize( ActionType.Ownership ) == 0 ) return;
		
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

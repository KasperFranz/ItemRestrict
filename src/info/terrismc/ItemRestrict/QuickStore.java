package info.terrismc.itemrestrict;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

public class QuickStore {
	final private ItemRestrict plugin;
	private ConfigStore cStore;
	
	public QuickStore( ItemRestrict plugin ) {
		this.plugin = plugin;
		this.cStore = plugin.cStore;
	}
	
	public void scanChunk( final Chunk chunk ) {
		if( cStore.getBanListSize( ActionType.World ) == 0 ) return;
		
		final BukkitScheduler scheduler = ItemRestrict.server.getScheduler();
		scheduler.runTaskAsynchronously( plugin, new Runnable() {
			public void run() {
				final int yMax = chunk.getWorld().getMaxHeight();
				Block block;
				for( int x = 0; x < 16; x++ ) {
					for( int z = 0; z < 16; z++ ) {
						for( int y = 0; y < yMax; y++ ) {
							block = chunk.getBlock( x, y, z );
							if( cStore.isBannable( null, block, ActionType.World ) ) {
								final Block clearBlock = block;
								ItemRestrict.logger.warning("Found bannable block");
								
								// Remove block synchronously
								scheduler.runTask( plugin , new Runnable() {
									public void run() {
										clearBlock.setType( Material.AIR );
									}
								});
							}
						}
					}
				}
			}
		});
	}
	
	public void scanInventory( final Player player ) {
		if( cStore.getBanListSize( ActionType.Ownership ) == 0 ) return;
		
		ItemRestrict.server.getScheduler().runTaskAsynchronously( plugin, new Runnable() {
			public void run() {
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				for( int i = 0; i < items.length; i++ )
					if( cStore.isBannable( player, items[i] , ActionType.Ownership ) )
						inventory.setItem( i, null );
			}
		});
	}
}
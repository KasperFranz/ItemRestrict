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
		
	public void notifyBan( final Player player, final ItemStack item ) {
		player.sendMessage( "Banned: " + cStore.getLabel( item ) );
		player.sendMessage( "Reason: " + cStore.getReason( item ) );
	}
	
	public void notifyBan( final Player player, final Block block ) {
		player.sendMessage( "Banned: " + cStore.getLabel( block ) );
		player.sendMessage( "Reason: " + cStore.getReason( block ) );
	}
	
	public void scanChunk( final Chunk chunk ) {
		if( cStore.getBanListSize( ActionType.World ) == 0 ) return;
		//at this point it should be ok, if it is just reading.
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
	
	public void itemUnequip( final Player player, final int itemSlot ) {
		
		int invSpace = 0;
		int freeSlot = 0;
		ItemStack item = player.getInventory().getItem(itemSlot);
		
		for (int i = 9; i <= 36; i++) {
			if ( player.getInventory().getItem(i) == null ) {
				invSpace = 1;
				freeSlot= i;
				break;
			}
		}
		if ( invSpace == 1 ) {
			player.getInventory().setItem( itemSlot, null );
			player.getInventory().setItem( freeSlot, item );
			notifyBan( player, item );
		}
		else {
			player.getInventory().setItem( itemSlot, null );
			player.getWorld().dropItemNaturally( player.getLocation(), item );
			player.sendMessage( "You are not allowed to equip this item and your internal inventory is full." );
		}
	}
	
	public void scanInventory( final Player player ) {
		if( cStore.getBanListSize( ActionType.Ownership ) == 0 && cStore.getBanListSize( ActionType.Equip ) == 0 ) return;
		//no need, only makes servers susceptible for crashes 
		//ItemRestrict.server.getScheduler().runTaskAsynchronously( plugin, new Runnable() {
			//public void run() {
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				if ( cStore.getBanListSize( ActionType.Ownership ) != 0 ) {
					for( int i = 0; i < items.length; i++ ) {
						if( cStore.isBannable( player, items[i] , ActionType.Ownership ) )
							inventory.setItem( i, null );
							//notifyBan( player, items[i] );
					}
				}
				
				if ( cStore.getBanListSize( ActionType.Equip ) != 0 ) {
					
					for(int i = 0; i <= 8; i++) {
						ItemStack item = player.getInventory().getItem(i);
						if( cStore.isBannable( player, item, ActionType.Equip ) ) {
							itemUnequip( player, i );
						}
					}
					for(int i = 36; i <= 39; i++) {
						ItemStack item = player.getInventory().getItem(i);
						if( cStore.isBannable( player, item, ActionType.Equip ) ) {
							itemUnequip( player, i );
						}
					}				
				}
			}

	public void flashItem( final Player player ) {
		final ItemStack item = player.getItemInHand();
		player.setItemInHand( null );
		plugin.getServer().getScheduler().runTaskLater( plugin, new Runnable() {
			public void run() {
				player.setItemInHand( item );
			}
		}, 1);
		
	}
}

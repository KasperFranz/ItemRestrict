package info.terrismc.itemrestrict;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
	private QuickStore qStore;
	private ConfigStore cStore;
	private Random rand;
	
	public EventListener( ItemRestrict plugin ) {
		this.cStore = plugin.cStore;
		this.qStore = plugin.qStore;
		rand = new Random();
	}
	
	
	// Ordered by ban type, Block/Entity/Player
	
	// Usage Bans - Prevent item usage
	@EventHandler( priority = EventPriority.LOWEST )
	public void onBlockPlace( BlockPlaceEvent event ) {
		// When a block is placed
		Player player = event.getPlayer();
		Block block = event.getBlock();
		ItemStack item = event.getItemInHand();

		// Check usage bannable and world
		if( cStore.isBannable( player, block, ActionType.Usage ) || cStore.isBannable( player, item, ActionType.Usage ) ) {
			// Cancel
			qStore.notifyBan( player, item );
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity( EntityDamageByEntityEvent event ) {
		// When an entity damages another entity
		Entity damager = event.getDamager();
		if( !( damager instanceof Player ) ) return;
		Player player = (Player) damager;
		ItemStack item = player.getItemInHand();

		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Usage ) ) return;
		
		// Cancel
		qStore.notifyBan( player, item );
		event.setCancelled( true );
	}
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// When a player interacts with world
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();

		// Check usage bannable and world
		if( cStore.isBannable( player, item, ActionType.Usage ) ) {
			qStore.notifyBan( player, item );
			event.setCancelled( true );
			qStore.flashItem( player );
		}
		else if( cStore.isBannable( player, block, ActionType.Usage ) && event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
			qStore.notifyBan( player, block );
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity( PlayerInteractEntityEvent event ) {
		// When a player interacts with an entity
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getItemInHand();

		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Usage ) ) return;
		
		// Cancel
		qStore.notifyBan( player, item );
		event.setCancelled( true );
	}
	
	// Crafting Bans - Prevent crafting when detected
	
	@EventHandler
	public void onItemCrafted( CraftItemEvent event ) {
		// When an item is crafted
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getRecipe().getResult();

		// Check ownership bannable and world
		if( cStore.isBannable( player, item, ActionType.Ownership ) || cStore.isBannable( player, item, ActionType.Crafting ) ) {
			// Cancel
			qStore.notifyBan( player, item );
			event.setCancelled( true );
		}
	}
	
	// Ownership Bans - Remove item when detected
	
	@EventHandler
	public void onPlayerJoin( PlayerJoinEvent event ) {
		// Perform random screening
		if( rand.nextDouble() > cStore.getScanFrequencyOnPlayerJoin() ) return;
		
		// When a player joins
		Player player = event.getPlayer();
		
		// Scan inventory
		qStore.scanInventory( player );
		
		// Scan chunk
		qStore.scanChunk( player.getLocation().getChunk() );
	}
	
	@EventHandler
	public void onInventoryClick( InventoryClickEvent event ) {
		// When an item is clicked in the inv
		Player player = (Player) event.getWhoClicked();
		ItemStack clickItem = event.getCurrentItem();
		ItemStack cursorItem = event.getCursor();
		
		// Check usage bannable and world
		if( cStore.isBannable( player, clickItem, ActionType.Ownership ) ) {
			// Cancel and ban
			qStore.notifyBan( player, clickItem );
			event.setCancelled( true );
			event.setCurrentItem( null );
		}
		
		// Check equip armour
		SlotType slotType = event.getSlotType();
		InventoryAction action = event.getAction();
		if( slotType == SlotType.ARMOR && isPlaceInventory( action ) && cStore.isBannable( player, cursorItem, ActionType.Usage ) ) {
			// Cancel
			qStore.notifyBan( player, cursorItem );
			event.setCancelled( true );
		}
		
		// Check equip armour
		if( slotType != SlotType.ARMOR && action == InventoryAction.MOVE_TO_OTHER_INVENTORY && cStore.isBannable( player, clickItem, ActionType.Usage ) ) {
			// Cancel
			qStore.notifyBan( player, clickItem );
			event.setCancelled( true );
		}
	}
	
	private boolean isPlaceInventory( InventoryAction action ) {
		switch( action ) {
		case PLACE_ALL:
		case PLACE_SOME:
		case PLACE_ONE:
			return true;
		default:
			return false;
		}
	}

	@EventHandler
	public void onPlayerPickupItem( PlayerPickupItemEvent event ) {
		// When a player pickups
		Player player = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();
		// Check ownership bannable and world
		if( cStore.isBannable( player, item, ActionType.Ownership ) ) {			
			// Cancel
			qStore.notifyBan( player, item );
			event.setCancelled( true );
		}
		else if ( cStore.isBannable( player, item, ActionType.Equip ) ) {	
			int invSpace = 0;
			int freeSlot = 0;
			for (int i = 9; i < 36; i++) {
				if ( player.getInventory().getItem(i) == null ) {
					invSpace = 1;
					freeSlot= i;
					break;
				}
			}
			if ( invSpace == 1 ) {
				qStore.notifyBan( player, item );
				event.getItem().remove();
				event.setCancelled( true );
				player.getInventory().setItem( freeSlot, item );
			}
			else {
				player.sendMessage( "You are not allowed to equip this item and your internal inventory is full." );
				event.setCancelled( true );
			}
		}
	}
	
	@EventHandler
	public void onInventory ( InventoryCloseEvent event ) {
		Player player = (Player) event.getPlayer();		
		// Scan inventory
		qStore.scanInventory( player );
	}
	
	@EventHandler
	public void onPlayerItemHeld( final PlayerItemHeldEvent event ) {
	// Don´t do this, this only causes server crashes, what happens if another plugin accesses the slot during it gets deleted ?
		//ItemRestrict.server.getScheduler().runTaskAsynchronously( plugin, new Runnable() {
		//	public void run() {
				// When a player switches item in hand
				Player player = event.getPlayer();
				int slotId = event.getNewSlot();
				ItemStack item = player.getInventory().getItem( slotId );;
				
				// Check ownership bannable and world
				if( item != null && cStore.isBannable( player, item, ActionType.Ownership ) ) {				
					// Ban
					qStore.notifyBan( player, item );
					player.getInventory().setItem( slotId, null );
				}
				else if( item != null && cStore.isBannable( player, item, ActionType.Equip ) ) {
					qStore.notifyBan( player, item );
					qStore.scanInventory( player );
					//qStore.itemUnequip( player, slotId );
					//player.getInventory().setItem( slotId, null );
					//player.getWorld().dropItemNaturally( player.getLocation(), item );
				}
			}
		//});
	//}
	
	// World Bans - Remove block when detected
	@EventHandler
	public void onChunkLoad( ChunkLoadEvent event ) {
		// Perform random screening
		if( rand.nextDouble() > cStore.getScanFrequencyOnChunkLoad() ) return;
		
		// When a chunk loads
		Chunk chunk = event.getChunk();
		
		// Scan chunk
		qStore.scanChunk( chunk );
	}
}

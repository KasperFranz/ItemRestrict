package info.terrismc.itemrestrict;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
	private ItemRestrict plugin;
	private QuickStore qStore;
	private ConfigStore cStore;
	private Random rand;
	
	public EventListener( ItemRestrict plugin ) {
		this.plugin = plugin;
		this.cStore = plugin.cStore;
		this.qStore = plugin.qStore;
		rand = new Random();
	}
	
	private void notifyBan( Player player, ItemStack item ) {
		player.sendMessage( "Banned: " + cStore.getLabel( item ) );
		player.sendMessage( "Reason: " + cStore.getReason( item ) );
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
			notifyBan( player, item );
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity( EntityDamageByEntityEvent event ) {
		// When a player interacts with world
		Entity damager = event.getDamager();
		if( !( damager instanceof Player ) ) return;
		Player player = (Player) damager;
		ItemStack item = player.getItemInHand();

		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Usage ) ) return;
		
		// Cancel
		notifyBan( player, item );
		event.setCancelled( true );
	}
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// When a player interacts with world
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Usage ) ) return;
		
		// Cancel
		notifyBan( player, item );
		event.setCancelled( true );
	}
	
	@EventHandler
	public void onPlayerInteractEntity( PlayerInteractEntityEvent event ) {
		// TODO Test for redundency
		// When a player interacts with an entity
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getItemInHand();

		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Usage ) ) return;
		
		// Cancel
		notifyBan( player, item );
		event.setCancelled( true );
	}
	
	// Ownership Bans - Remove item when detected
	
	@EventHandler
	public void onItemCrafted( CraftItemEvent event ) {
		// When an item is crafted
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getRecipe().getResult();

		// Check ownership bannable and world
		if( !cStore.isBannable( player, item, ActionType.Ownership ) ) return;
		
		// Cancel
		notifyBan( player, item );
		event.setCancelled( true );
	}
	
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
		ItemStack item = event.getCurrentItem();
		
		// Check usage bannable and world
		if( !cStore.isBannable( player, item, ActionType.Ownership ) ) return;

		// Cancel and ban
		notifyBan( player, item );
		event.setCancelled( true );
		event.setCurrentItem( null );
	}

	@EventHandler
	public void onPlayerPickupItem( PlayerPickupItemEvent event ) {
		// When a player pickups
		Player player = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();

		// Check ownership bannable and world
		if( !cStore.isBannable( player, item, ActionType.Ownership ) ) return;
		
		// Cancelw
		notifyBan( player, item );
		event.setCancelled( true );
	}
	
	@EventHandler
	public void onPlayerItemHeld( final PlayerItemHeldEvent event ) {
		ItemRestrict.server.getScheduler().runTaskAsynchronously( plugin, new Runnable() {
			public void run() {
				// When a player switches item in hand
				Player player = event.getPlayer();
				int slotId = event.getNewSlot();
				ItemStack item = player.getInventory().getItem( slotId );;
				
				// Check ownership bannable and world
				if( !cStore.isBannable( player, item, ActionType.Ownership ) ) return;
				
				// Ban
				notifyBan( player, item );
				player.getInventory().setItem( slotId, null );
			}
		});
	}
	
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

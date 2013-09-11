package info.terrismc.TekkitCustomizer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EventListener {
	TekkitCustomizer plugin;
	QuickStore qStore;
	ConfigStore cStore;
	
	public EventListener( TekkitCustomizer plugin ) {
		this.cStore = plugin.cStore;
		this.qStore = plugin.qStore;
	}
	
	private boolean isBannable( ItemStack item, ActionType actionType, World world ) {
		// Check world
		if( cStore.isEnabledWorld( world ) ) return false;
		
		// Check banned
		return cStore.isBanned(item, actionType);
	}
	
	private boolean isBannable( Block block, ActionType actionType ) {
		// Check world
		if( cStore.isEnabledWorld( block.getWorld() ) ) return false;
		
		// Check banned
		return cStore.isBanned( block, actionType );
	}
	
	// Ordered by ban type, Block/Entity/Player
	
	// Usage Bans - Prevent item usage
	@EventHandler( priority = EventPriority.LOWEST )
	public void onBlockPlace( BlockPlaceEvent event ) {
		// When a block is placed
		Block block = event.getBlock();

		// Check usage bannable and world
		if( isBannable( block, ActionType.Ownership ) ) {
			// Cancel and notify
			event.setCancelled( true );
		}

		// Check usage bannable and world
		if( isBannable( block, ActionType.Usage ) ) {
			event.setCancelled( true );
			
		}
	}
	
	public void onEntityDamageByEntity( EntityDamageByEntityEvent event ) {
		event.getDamager();
	}
	
	void onPlayerInteract( PlayerInteractEvent event ) {
		// When a player interacts with world
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		// Check usage bannable and world
		if( !isBannable( item, ActionType.Usage, player.getWorld() ) ) return;
		
		// Cancel and notify
		event.setCancelled( true );
	}
	
	void onPlayerInteractEntity( PlayerInteractEntityEvent event ) {
		// TODO Test for redundency
		// When a player interacts with an entity
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getItemInHand();

		// Check usage bannable and world
		if( !isBannable( item, ActionType.Usage, player.getWorld() ) ) return;
		
		// Cancel and notify
		event.setCancelled( true );
	}
	
	// Ownership Bans - Remove item when detected
	
	void onItemCrafted( CraftItemEvent event ) {
		// When an item is crafted
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getRecipe().getResult();

		// Check ownership bannable and world
		if( !isBannable( item, ActionType.Ownership, player.getWorld() ) ) return;
		
		// Cancel and notify
		event.setCancelled( true );
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		// When a player joins, check inv
		Player player = event.getPlayer();

		// Check ownership bannable and world
		if( cStore.isEnabledWorld( player.getWorld() ) ) return;
		
		// Confiscate bannables using a scan
	}
	
	void onItemClicked( InventoryClickEvent event ) {
		// When an item is clicked in the inv
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		
		// Check usage bannable and world
		if( !isBannable( item, ActionType.Ownership, player.getWorld() ) ) return;

		// Cancel and notify
		event.setCancelled( true );
		if( event.getInventory() instanceof PlayerInventory ) {
			item.setType( Material.AIR );
			player.sendMessage( "Banned item confiscated.  Reason: " );
		}
		else
			player.sendMessage( "Sorry, that item is banned.  Reason: " );
	}
	
	void onPlayerPickupItem( PlayerPickupItemEvent event ) {
		// When a player pickups
		Player player = event.getPlayer();
		ItemStack item = event.getItem().getItemStack();

		// Check ownership bannable and world
		if( !isBannable( item, ActionType.Ownership, player.getWorld() ) ) return;
		
		// Cancel and notify
		event.setCancelled( true );
	}
	
	void onPlayerSwitchInHand( PlayerItemHeldEvent event ) {
		// When a player switches item in hand
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();

		// Check ownership bannable and world
		if( !isBannable( item, ActionType.Ownership, player.getWorld() ) ) return;
		
		// Confiscate bannables
	}
}

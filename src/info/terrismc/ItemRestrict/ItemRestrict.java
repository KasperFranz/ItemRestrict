package info.terrismc.itemrestrict;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;


public class ItemRestrict extends JavaPlugin {
	
	// Create static instances
	public static Logger logger;
	public static Server server;

	// Create storge interfaces
	public ConfigStore cStore;
	public QuickStore qStore;
	
	public void onEnable() {
		// Initialize static objects
		logger = getLogger();
		server = getServer();
		
		// Initialize storge interfaces
		cStore = new ConfigStore( this );
		qStore = new QuickStore( this );
		
		// Register event handler
		this.getServer().getPluginManager().registerEvents( new EventListener( this ), this );
		
		// Register command handler
		getCommand( "ires" ).setExecutor( new CommandListener( this ) );
	}
}
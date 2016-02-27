package info.terrismc.itemrestrict;

import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;


public class ItemRestrict extends JavaPlugin {
	
	// Create static instances
	public static Logger logger;
	public static Server server;

	// Create storge interfaces
	public ConfigStore cStore;
	public QuickStore qStore;
	
        @Override
	public void onEnable() {
		// Initialize static objects
		logger = getLogger();
		server = getServer();
		
		// Initialize storge interfaces
		cStore = new ConfigStore( this );
		qStore = new QuickStore( this );
		
		// Register event handler
		server.getPluginManager().registerEvents( new EventListener( this ), this );
		
		// Register command handler
		this.getCommand( "ires" ).setExecutor( new CommandListener( this ) );
                
                //MCStats
                try {
                    MetricsLite metrics = new MetricsLite(this);
                    metrics.start();
                } catch (IOException e) {}
	}
}
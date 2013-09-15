/*
    TekkitCustomizer Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.terrismc.TekkitCustomizer;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;


public class TekkitCustomizer extends JavaPlugin {
	
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
		
		this.getServer().getPluginManager().registerEvents( new EventListener( this ), this );
	}
}
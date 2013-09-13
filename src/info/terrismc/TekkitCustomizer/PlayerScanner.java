package info.terrismc.TekkitCustomizer;

public class PlayerScanner {
	private TekkitCustomizer plugin;
	private ConfigStore cStore;
	
	public PlayerScanner( TekkitCustomizer plugin ) {
		this.plugin = plugin;
		this.cStore = plugin.cStore;
	}
}
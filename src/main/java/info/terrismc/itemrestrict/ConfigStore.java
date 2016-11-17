package info.terrismc.itemrestrict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ConfigStore {

    private final ItemRestrict plugin;
    private FileConfiguration config;

    // Cache config values
    private List<String> worldList;
    private final Map<ActionType, List<String>> bans = new HashMap<>();

    public ConfigStore(ItemRestrict plugin) {
        this.plugin = plugin;

        // Force reload plugin
        reloadConfig();
    }

    public void reloadConfig() {
        // Config operations
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Config variables
        config = plugin.getConfig();
        worldList = config.getStringList("Worlds");

        for(final ActionType type : ActionType.values())
        {
        	if(!bans.containsKey(type.name()))
        	{
        		bans.put(type, new ArrayList<String>());
        	}

        	final List<String> values = bans.get(type);
        	values.clear();
        	values.addAll(toLowerCase(config.getStringList("Bans." + type.name())));
        }
    }

    public boolean hasPermission(CommandSender sender, String node, boolean allowConsole) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(node)) {
                sender.sendMessage("Insufficient permissions");
                return false;
            }
        } else {
            if (!allowConsole) {
                sender.sendMessage("This is only a player command");
            }
            return false;
        }

        return true;
    }

    public boolean isEnabledWorld(World world) {
        return worldList.contains("All") || worldList.contains(world.getName());
    }

    public boolean isBanned(Block block, ActionType actionType) {
        boolean banned = isBanned(getConfigString(block), actionType);
        if (!banned) {
            banned = isBanned(getConfigStringParent(block), actionType);
        }
        return banned;
    }

    public boolean isBanned(ItemStack item, ActionType actionType) {
        boolean banned = isBanned(getConfigString(item), actionType);
        if (!banned) {
            banned = isBanned(getConfigStringParent(item), actionType);
        }
        return banned;
    }

    private boolean isBanned(final String configString, final ActionType actionType)
    {
    	return bans.containsKey(actionType) && bans.get(actionType).contains(configString.toLowerCase());
    }

    public boolean isBannable(Player player, ItemStack item, ActionType actionType) {
        // Check null
        if (item == null) {
            return false;
        }

        // Player checks
        if (player != null) {
            // Check world
            if (!isEnabledWorld(player.getWorld())) {
                return false;
            }

            // Check exclude permission
            if (player.hasPermission("ItemRestrict.bypass." + getActionTypeString(actionType) + "." + getConfigString(item))) {
                return false;
            }

            // Check exclude parent permission
            if (player.hasPermission("ItemRestrict.bypass." + getActionTypeString(actionType) + "." + getConfigStringParent(item))) {
                return false;
            }
        }

        // Check ban list
        return isBanned(item, actionType);
    }

    public boolean isBannable(Player player, Block block, ActionType actionType) {
        // Check null
        if (block == null) {
            return false;
        }

        // Player checks
        if (player != null) {
            // Check world
            if (!isEnabledWorld(player.getWorld())) {
                return false;
            }

            // Check exclude permission
            if (player.hasPermission("ItemRestrict.bypass." + getActionTypeString(actionType) + "." + getConfigString(block))) {
                return false;
            }

            // Check exclude parent permission
            if (player.hasPermission("ItemRestrict.bypass." + getActionTypeString(actionType) + "." + getConfigStringParent(block))) {
                return false;
            }
        }

        // Check ban list
        return isBanned(block, actionType);
    }

    public String getLabel(Block block) {
        String label = config.getString("Messages.labels." + getConfigString(block));
        if (label != null) {
            return label.replace('&', '\u00A7');
        }
        label = config.getString("Messages.labels." + getConfigStringParent(block));
        if (label != null) {
            return label.replace('&', '\u00A7');
        }
        return block.getType().name() + " (" + getConfigString(block) + ")";
    }

    public String getLabel(ItemStack item) {
        String label = config.getString("Messages.labels." + getConfigString(item));
        if (label != null) {
            return label.replace('&', '\u00A7');
        }
        label = config.getString("Messages.labels." + getConfigStringParent(item));
        if (label != null) {
            return label.replace('&', '\u00A7');
        }
        return getConfigString(item);
    }

    public String getReason(Block block) {
        String reason = config.getString("Messages.reasons." + getConfigString(block));
        if (reason != null) {
            return reason.replace('&', '\u00A7');
        }
        reason = config.getString("Messages.reasons." + getConfigStringParent(block));
        if (reason != null) {
            return reason.replace('&', '\u00A7');
        }
        return "Ask your server administrator.";
    }

    public String getReason(ItemStack item) {
        String reason = config.getString("Messages.reasons." + getConfigString(item));
        if (reason != null) {
            return reason.replace('&', '\u00A7');
        }
        reason = config.getString("Messages.reasons." + getConfigStringParent(item));
        if (reason != null) {
            return reason.replace('&', '\u00A7');
        }
        return "Ask your server administrator.";
    }

    private String getActionTypeString(ActionType actionType) {
        // Select proper string
        switch (actionType) {
            case Usage:
                return "Usage";
            case Equip:
                return "Equip";
            case Crafting:
                return "Crafting";
            case Ownership:
                return "Ownership";
            case World:
                return "World";
            default:
                // Should never reach here if all enum cases covered
                ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", actionType.toString());
                return "";
        }
    }

    /** \brief Checks if a string validates as configuration string
     * 
     * Checks if a string validates as configuration string.
     * A configuration string consists of a string followed by an optional hyphen '-' and a number
     * Example: WOOL-5
     * 
     * @param config string to check
     * @return true if strConfig is a configuration string, false otherwise
     */
    private boolean isConfigString(final String config)
    {
    	final int dashIndex = config.indexOf('-');
    	final String name = (dashIndex < 0 ? config : config.substring(0, dashIndex));
    	final String dataValue = (dashIndex < 0 ? "0" : config.substring(dashIndex + 1));

    	// TODO (k4su: 07.11.16): eventually replace by regex
    	try
    	{
    		Integer.parseInt(dataValue);
    	}
    	catch(final NumberFormatException exception)
    	{
    		return false;
    	}

    	return (name.length() > 0);
    }



    @SuppressWarnings("deprecation")
    private String getConfigStringParent(Block block) {
        // Config version string of block id 
        return ("" + block.getTypeId()).toLowerCase();
    }
    
     @SuppressWarnings("deprecation")
    private String getConfigString(Block block) {
        // Config version string of item id and data value
        
        return ("" + block.getType().toString() + "-" + block.getData()).toLowerCase();
    }
    
    @SuppressWarnings("deprecation")
    private String getConfigString(ItemStack item) {
        // Config version string of item id and data value
        MaterialData matData = item.getData();
        return ("" + item.getType().toString() + "-" + matData.getData()).toLowerCase();
    }

    private String getConfigStringParent(ItemStack item) {
        // Config version string of item id and data value
        return ("" + item.getType().toString()).toLowerCase();
    }

    public double getScanFrequencyOnPlayerJoin() {
        return config.getDouble("Scanner.event.onPlayerJoin");
    }

    public double getScanFrequencyOnChunkLoad() {
        return config.getDouble("Scanner.event.onChunkLoad");
    }

    /** \brief get the size of bans for the actiontype
     * 
     * @param actionType actiontype to get banlength
     * @return amount of bans for xActionType
     */
    public int getBanListSize(final ActionType actionType)
    {
    	if(bans.containsKey(actionType))
    	{
    		return bans.get(actionType).size();
    	}
    	else
    	{
    		ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", actionType.name());
    		return 0;
    	}
    }

    /** \brief add a ban for a specified usage
     * 
     * @param sender          sender who called the command
     * @param actionType      usage type for the ban
     * @param configString  name of the item (incl. data after dash '-')
     */
    public void addBan(final CommandSender sender, final ActionType actionType, final String configString) {
        // Check valid actionType
        if (actionType == null) {
            sender.sendMessage("Invalid ban type. Valid ban types: Usage, Equip, Crafting, Ownership, World");
            return;
        }

        // Check valid config string
        if (!isConfigString(configString)) {
            sender.sendMessage(configString + "  is not a valid item");
            return;
        }

        if(bans.containsKey(actionType))
        {
			final List<String> typeBans = bans.get(actionType);
			// only add if not yet added
			if(!typeBans.contains(configString))
			{
				typeBans.add(configString.toLowerCase());
			}
			config.set("Bans." + actionType.name(), typeBans);
        }
        else
        {
            // Should never reach here if all enum cases covered
            ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", actionType.toString());
            return;
        }
        plugin.saveConfig();
        sender.sendMessage("Item Banned");
    }

    /** \brief remove a ban for a specified usage
     * 
     * @param sender          sender who called the command
     * @param actionType      usage type for the ban
     * @param configString  name of the item (incl. data after dash '-')
     */
    public void removeBan(final CommandSender sender, final ActionType actionType, final String configString) {
        // Check valid actionType
        if (actionType == null) {
            sender.sendMessage("Invalid ban type. Valid ban types: Usage, Equip, Crafting, Ownership, World");
            return;
        }

        // Check valid config string
        if (!isConfigString(configString)) {
            sender.sendMessage(configString + " is not a valid item");
            return;
        }

        if(bans.containsKey(actionType))
        {
			final List<String> typeBans = bans.get(actionType);
			typeBans.remove(configString.toLowerCase());
			config.set("Bans." + actionType.name(), typeBans);
        }
        else
        {
            // Should never reach here if all enum cases covered
            ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", actionType.toString());
            return;
        }

        plugin.saveConfig();
        sender.sendMessage("Item Unbanned");
    }

    /** \brief Converts eventual ids stored in the banlists to their material type */
    public void convert()
    {
    	for(final List<String> banList : bans.values())
    	{
    		final int banCount = banList.size();
    		for(int i = 0; i < banCount; ++i)
    		{
    			final String value = banList.get(i);
    			final int dashIndex = value.indexOf('-');

    			int id = -1;
    			int data = -1;
    			if(dashIndex == -1)  // no data value, eg. >>pink<< wool
    			{
    				try
    				{
    					id = Integer.parseInt(value);
    				}
    				catch(final NumberFormatException e)
    				{
    					ItemRestrict.logger.log(Level.WARNING, "Skip item: " + value);
    					continue;
    				}
    			}
    			else
    			{
    				try
    				{
    					id = Integer.parseInt(value.substring(0, dashIndex));
    					data = Integer.parseInt(value.substring(dashIndex + 1));
    				}
    				catch(final NumberFormatException e)
    				{
    					ItemRestrict.logger.log(Level.WARNING, "Skip item: " + value);
    					continue;
    				}
    			}


				final Material material = Material.getMaterial(id);
				if(material != null)
				{
					if(data >= 0)
					{
						banList.set(i, material.name().toLowerCase() + '-' + data);
					}
					else
					{
						banList.set(i, material.name().toLowerCase());
					}
				}
				else
				{
					ItemRestrict.logger.log(Level.WARNING, "Material not found for id: " + value);
					continue;
				}
    		}
    	}
    	saveBans();
    }

    /** \brief saves the bans to the configfile */
    public void saveBans()
    {
    	for(final ActionType actionType : bans.keySet())
    	{
    		config.set("Bans." + actionType.name(), bans.get(actionType));
    	}
    	plugin.saveConfig();
    }

    public void getInformationInHand(CommandSender sender) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            sender.sendMessage(getConfigString(player.getItemInHand()));
        }else{
            sender.sendMessage("you need to be a player to get this information");
        }
    }

    private List<String> toLowerCase(List<String> stringList) {
        for(int i = 0; i<stringList.size(); i++){
            stringList.set(i, stringList.get(i).toLowerCase());
        }
        return stringList;
    }
}

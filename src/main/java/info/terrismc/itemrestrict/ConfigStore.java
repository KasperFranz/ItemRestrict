package info.terrismc.itemrestrict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

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
    private final Map<ActionType, List<String>> k_mapBans = new HashMap<>();

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

        for(final ActionType xType : ActionType.values())
        {
        	if(!k_mapBans.containsKey(xType.name()))
        	{
        		k_mapBans.put(xType, new ArrayList<String>());
        	}

        	final List<String> lstValues = k_mapBans.get(xType);
        	lstValues.clear();
        	lstValues.addAll(toLowerCase(config.getStringList("Bans." + xType.name())));
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

    private boolean isBanned(final String strConfigString, final ActionType xActionType)
    {
    	return k_mapBans.containsKey(xActionType) && k_mapBans.get(xActionType).contains(strConfigString.toLowerCase());
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
     * @param strConfig string to check
     * @return true if strConfig is a configuration string, false otherwise
     */
    private boolean isConfigString(final String strConfig)
    {
    	final int iDashIndex = strConfig.indexOf('-');
    	final String strName = (iDashIndex < 0 ? strConfig : strConfig.substring(0, iDashIndex));
    	final String strDataValue = (iDashIndex < 0 ? "0" : strConfig.substring(iDashIndex + 1));

    	// TODO (k4su: 07.11.16): eventually replace by regex
    	try
    	{
    		Integer.parseInt(strDataValue);
    	}
    	catch(final NumberFormatException xException)
    	{
    		return false;
    	}

    	return (strName.length() > 0);
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

    @SuppressWarnings("deprecation")
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
     * @param xActionType actiontype to get banlength
     * @return amount of bans for xActionType
     */
    public int getBanListSize(final ActionType xActionType)
    {
    	if(k_mapBans.containsKey(xActionType))
    	{
    		return k_mapBans.get(xActionType).size();
    	}
    	else
    	{
    		ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", xActionType.name());
    		return 0;
    	}
    }

    /** \brief add a ban for a specified usage
     * 
     * @param xSender          sender who called the command
     * @param xActionType      usage type for the ban
     * @param strConfigString  name of the item (incl. data after dash '-')
     */
    public void addBan(final CommandSender xSender, final ActionType xActionType, final String strConfigString) {
        // Check valid actionType
        if (xActionType == null) {
            xSender.sendMessage("Invalid ban type. Valid ban types: Usage, Equip, Crafting, Ownership, World");
            return;
        }

        // Check valid config string
        if (!isConfigString(strConfigString)) {
            xSender.sendMessage(strConfigString + "  is not a valid item");
            return;
        }

        if(k_mapBans.containsKey(xActionType))
        {
			final List<String> lstBans = k_mapBans.get(xActionType);
			// only add if not yet added
			if(!lstBans.contains(strConfigString))
			{
				lstBans.add(strConfigString.toLowerCase());
			}
			config.set("Bans." + xActionType.name(), lstBans);
        }
        else
        {
            // Should never reach here if all enum cases covered
            ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", xActionType.toString());
            return;
        }
        plugin.saveConfig();
        xSender.sendMessage("Item Banned");
    }

    /** \brief remove a ban for a specified usage
     * 
     * @param xSender          sender who called the command
     * @param xActionType      usage type for the ban
     * @param strConfigString  name of the item (incl. data after dash '-')
     */
    public void removeBan(final CommandSender xSender, final ActionType xActionType, final String strConfigString) {
        // Check valid actionType
        if (xActionType == null) {
            xSender.sendMessage("Invalid ban type. Valid ban types: Usage, Equip, Crafting, Ownership, World");
            return;
        }

        // Check valid config string
        if (!isConfigString(strConfigString)) {
            xSender.sendMessage(strConfigString + " is not a valid item");
            return;
        }

        if(k_mapBans.containsKey(xActionType))
        {
			final List<String> lstBans = k_mapBans.get(xActionType);
			lstBans.remove(strConfigString.toLowerCase());
			config.set("Bans." + xActionType.name(), lstBans);
        }
        else
        {
            // Should never reach here if all enum cases covered
            ItemRestrict.logger.log(Level.WARNING, "Unknown ActionType detected: {0}", xActionType.toString());
            return;
        }

        plugin.saveConfig();
        xSender.sendMessage("Item Unbanned");
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

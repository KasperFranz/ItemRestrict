Use this plugin to ban items and blocks for Bukkit, Tekkit, and FTB servers using the Bukkit API. ItemRestrict removes banned items from the world and player inventories. It is easy to configure, can be applied retroactively, and automatically removes the items. It requires no permissions plugin unless exceptions are need for certain players.  **It is compatible with 1.2.5, 1.3.2, 1.4.7, 1.5.2, 1.6.4, 1.7.10.**

_Because every story that starts with &quot;We decided to make a change...&quot; shouldn&#39;t end with &quot;...so we had to reset the world.&quot;_

# Installation

Copy the .jar file into your plugins folder to install the plugin. Then restart the server or use a plugin manager to load the plugin.

# Commands

Use the command &quot;/ires reload&quot; to reload the config file.

| **Command** | **Permission Node** | **Description** |
| --- | --- | --- |
| /ires reload | itemrestrict.reload | Reload the config |
| /ires hand | itemrestrict.hand | gives you the name (and datavalue) of the item in hand |
| /ires ban \<banType> \<name-datavalue> | itemrestrict.ban | Add item to the ban list |
| /ires unban \<banType> \<name-datavalue> | itemrestrict.ban | Remove item from the ban list |

# Configuration

There are 4 major sections in the cofig file (config.yml). Enabled worlds are listed under Worlds. World Ban scanner settings is under Scanner. Ban settings are under Bans. Notification settings to the player are under Messages. A default config for  [**Tekkit**](http://dev.bukkit.org/paste/8649/) was generously created by BigScary.

Worlds listed in the Worlds section are enabled worlds which will be checked by the plugin. You can name normal worlds, dimensions, and mystcraft worlds by using the world&#39;s folder name. Add the case sensitive world called &quot;All&quot; to bypass the world check.

The Scanner settings indicate the percentage chance (from 0 to 1) that it scans a loaded player or a loaded chunk.

You can specify the items you wish to ban under the Bans section. Take a look at the different types of bans below. An entry with only the item id (32) will specify ALL wool (all datavalues). An entry with the item id and datavalue separated by a dash (32-2) specifies MAGENTA wool (datavalue 2). Some items have a different values in your inventory as opposed to ones place in the world (eg. a door item in the inventory versus a placed door in the world). A full list of  [**Block IDs**](http://minecraft-ids.grahamedgecombe.com/) will help you add banned items.

The label gives the item a name when notifying the player. The reason provides the reason of why the item was banned for them. You can use colour with both the label and the reason by using the escape character &quot;&amp;&quot; and a hex number corresponding to the  [**minecraft colour**](http://ess.khhq.net/mc/).

# Permissions

## Making Ban Exceptions

You can make individual ban exceptions to give some players to generally banned items through permission nodes. A player who has /op automatically ignores bans. Specifying the item ID without the datavalue means all data values (-\*). Here are various examples, which should help you understand without further explanation.

- ItemRestrict.bypass.usage.35

- ItemRestrict.bypass.ownership.35-6

You can&#39;t prevent a specific player&#39;s placed block from being removed from the world as this plugin does not track who owns placed blocks in the world.

# Types of Bans

## Usage Bans

This ban prevents players from using an item, which means left-click or right-click while the item is in-hand. This ban also prevents players from right clicking a block specified by this ban. This ban also prevents a player from building a block in the world. This works well for items like the Mining Laser, but won&#39;t stop players from using items which benefit from passive use (eg. modded armors, Talisman of Repair, etc.). You will need an ownership ban to prevent players from getting those items at all. A player that already has an item which is later placement-banned can still trade it with another player or break it down into energy (EE mod) to get some value out of it.

It&#39;s not possible for any plugin to prevent a player from using an item when it is activated by a keystroke (eg. R, G, C, etc.), which are common especially for Equivalent Exchange items. This is why many EE items are on the default ownership bans list even though their functions are not all really ban-worthy.

It is recommend to place most or all usage ban items to your ownership ban list. This will prevent your players from unknowingly spending their resources to craft a block which they then can&#39;t place in the world like they planned leading to them feeling cheated and likely complaining. The only exception you might make to this rule is for an item which can&#39;t be placed, but is still useful because it&#39;s an ingredient in another recipe for an item you haven&#39;t banned.

## Ownership Bans

This ban prevents player from picking up an item from the ground or from a container (eg. a chest). If a player doesn&#39;t have permission to own an item, they won&#39;t be able to craft it. The contraband scanner will search each player&#39;s inventory for ownership-banned items and actually remove them without warning, notification, or compensation.

This ban also prevents players from crafting an item. When the player tries to craft the item, they get a message that they can&#39;t. This is the most player-friendly approach to preventing players from getting banned items. Forge mods don&#39;t always hook into Bukkit, meaning its possible for players to get banned items through some automatic or non-crafting approach (eg. alchemy). When you crafting-ban an item, test for workarounds, which may lead you to ban additional items to prevent those workarounds.

## World Bans

This ban will retroactively remove the placed block from the world through the Asynchronous World Scanner (AWS). This is great if you need to ban an item, but also know it&#39;s in the world (eg. energy collectors and world anchors). This will help clean up newly-banned items without having to search your entire world for them. There are exceptions to this removal as the scanner can&#39;t determine who owns the block.

# Notes

This plugin uses the idea from  [**BigScary&#39;s TekkitCustomizer**](https://github.com/ryanhamshire/TekkitCustomizer). However all components in his code have been replaced with high performing and simple implementations. As a server administrator, I care about performance as much as you. Thus no code is actually used from the TekkitCustomizer plugin and the source code can be found  [**here**](https://bitbucket.org/krisdestruction/itemrestrict).

The default ban list is made for Classic Tekkit based on hands on testing done by BigScary. Before deciding to remove items from this list, be sure to read the  [**reasons for default bans**](https://dev.bukkit.org/bukkit-plugins/item-restrict/pages/default-bans/) to understand why it was originally removed. If you are running FTB, Tekkit Lite, or other mod packs, do your own research for what you want banned and change the config file accordingly.

Save yourself some time! Check our Frequently Asked Questions page for the answer to your question. If you found a bug, make a ticket and include your config file.

# Reviews

Spanish

# Supporters

Thank you to piritacraft for testing each ticketed issue. Thank you to excavator5 for testing the plugin.

Thank you to our contributers that have helped contribute to this project:

- Slind14 from  [net](http://mineyourmind.net/)
- maxmar628

Thank you to our supporters that have generously donated to ItemRestrict:

- Thundercoyote

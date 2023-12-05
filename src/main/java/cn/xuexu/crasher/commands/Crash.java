package cn.xuexu.crasher.commands;

import cn.xuexu.crasher.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Crash implements TabExecutor {
    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (!commandSender.hasPermission("crasher.admin")) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandNoPermission")));
            return false;
        }
        if (strings.length != 2) {
            if (strings[0].equalsIgnoreCase("reload")) {
                Utils.instance.saveDefaultConfig();
                Utils.instance.reloadConfig();
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandDone")));
                return true;
            } else {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /Crash <player(TAB)> <type(TAB)>"));
                return false;
            }
        }
        if (Bukkit.getPlayer(strings[0]) == null) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandPlayerNotOnline")));
        }
        final Player player = Bukkit.getPlayer(strings[0]);
        if (strings[1].equalsIgnoreCase("cancel_packets")) {
            if (!Utils.crashSet.contains(player.getUniqueId())) {
                Utils.crashPlayer(player, 1);
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandDone")));
            } else {
                Utils.removeCrashSet(player.getUniqueId());
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandRemoveCrashList")));
            }
            return true;
        } else if (strings[1].equalsIgnoreCase("entitys")) {
            Utils.crashPlayer(player, 2);
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandDone")));
            return true;
        } else if (strings[1].equalsIgnoreCase("explosions")) {
            Utils.crashPlayer(player, 3);
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandDone")));
            return true;
        } else if (strings[1].equalsIgnoreCase("frozen")) {
            if (Utils.frozenSet.contains(player.getUniqueId())) {
                if (!player.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) || !player.getOpenInventory().getType().equals(InventoryType.CHEST)) {
                    Utils.frozenSet.remove(player.getUniqueId());
                    player.closeInventory();
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandFrozenRemove")));
                    Utils.instance.getLogger().severe("ERROR CODE: 0004. IS OTHER PLUGIN USEING API?");
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.ERROR")));
                } else {
                    Utils.frozenSet.remove(player.getUniqueId());
                    player.closeInventory();
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandFrozenRemove")));
                }
            } else {
                Utils.frozenPlayer(player);
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.CommandDone")));
            }
            return true;
        } else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /Crash <player(TAB)> <type(TAB)>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (commandSender.hasPermission("crasher.admin")) {
            if (strings.length == 2) {
                final List<String> completions = Arrays.asList("cancel_packets", "entitys", "explosions", "frozen", "reload");
                final List<String> returnList = new ArrayList<>();
                for (String start : completions) {
                    if (start.startsWith(strings[1])) {
                        returnList.add(start);
                    }
                }
                return returnList;
            }
        }
        return null;
    }
}

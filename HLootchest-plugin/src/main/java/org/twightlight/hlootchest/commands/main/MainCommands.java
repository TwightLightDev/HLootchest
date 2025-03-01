package org.twightlight.hlootchest.commands.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.sessions.LootChestSessions;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Set;

public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utility.info("This command must be executed by a player!");
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                Utility.sendHelp(p, "player");
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "leave":
                        if (!p.hasPermission("hlc."+args[0])) {
                            p.sendMessage(Utility.getMsg(p, "noPerms"));
                        }
                        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
                            HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p).close();
                        }
                        return true;
                    case "open":
                        if (args.length < 2) {
                            p.sendMessage(ChatColor.RED + "Please enter lootchest type!");
                            return true;
                        }
                        String type = args[1].toLowerCase();
                        if (!HLootchest.getNms().getRegistrationData().containsKey(type)) {
                            p.sendMessage(Utility.getMsg(p, "lootchestNotFound"));
                            return true;
                        }
                        if (!p.hasPermission("hlc." + args[0] + "." + args[1])) {
                            p.sendMessage(Utility.getMsg(p, "noPerms"));
                        }
                        new LootChestSessions(p, type);
                        return true;
                    case "list":
                        if (!p.hasPermission("hlc."+args[0])) {
                            p.sendMessage(Utility.getMsg(p, "noPerms"));
                        }
                        Set<String> types = HLootchest.getNms().getRegistrationData().keySet();
                        p.sendMessage(ChatColor.GREEN + "Available lootchest types:");
                        for (String type1 : types) {
                            p.sendMessage("- " + type1);
                        }
                        return true;
                    default:
                        Utility.sendHelp(p, "player");
                        return true;
                }
            }
        }
        return true;
    }
}



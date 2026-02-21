package org.twightlight.hlootchest.commands.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.sessions.LootChestSession;
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
                HLootChest.getAPI().getLanguageUtil().sendHelp(p, "player");
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "leave":
                        if (!p.hasPermission("hlc."+args[0])) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "noPerms"));
                        }
                        if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
                            HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p).close();
                        }
                        return true;
                    case "open":
                        if (args.length < 2) {
                            p.sendMessage(ChatColor.RED + "Please enter lootchest type!");
                            return true;
                        }
                        String type = args[1].toLowerCase();
                        if (!HLootChest.getNms().getRegistrationData().containsKey(type)) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "lootchestNotFound"));
                            return true;
                        }
                        if (!p.hasPermission("hlc." + args[0] + "." + args[1])) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "noPerms"));
                            return true;
                        }
                        new LootChestSession(p, type);
                        return true;
                    case "forceopen":
                        if (args.length < 2) {
                            p.sendMessage(ChatColor.RED + "Please enter lootchest type!");
                            return true;
                        }
                        String type1 = args[1].toLowerCase();
                        if (!HLootChest.getNms().getRegistrationData().containsKey(type1)) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "lootchestNotFound"));
                            return true;
                        }
                        if (!p.hasPermission("hlc." + args[0] + "." + args[1])) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "noPerms"));
                            return true;
                        }
                        LootChestSession session = new LootChestSession(p, type1);
                        session.getLoadingCompletableFuture().thenApply((bool) -> {
                            session.forceOpen();
                            return bool;
                        });
                        return true;
                    case "list":
                        if (!p.hasPermission("hlc."+args[0])) {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "noPerms"));
                        }
                        Set<String> types = HLootChest.getNms().getRegistrationData().keySet();
                        p.sendMessage(ChatColor.GREEN + "Available lootchest types:");
                        for (String type2 : types) {
                            p.sendMessage("- " + type2);
                        }
                        return true;
                    default:
                        HLootChest.getAPI().getLanguageUtil().sendHelp(p, "player");
                        return true;
                }
            }
        }
        return true;
    }
}



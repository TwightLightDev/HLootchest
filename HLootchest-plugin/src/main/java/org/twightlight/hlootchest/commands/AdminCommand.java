package org.twightlight.hlootchest.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;

public class AdminCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("hlc.admin")) {
                p.sendMessage(Utility.getMsg(p, "noPerms"));
            }
            if (args.length < 1) {
                Utility.sendHelp(p, "admin");
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        HLootchest.getAPI().getConfigUtil().getTemplateConfig().reload();
                        HLootchest.getAPI().getConfigUtil().getBoxesConfig().reload();
                        HLootchest.getAPI().getConfigUtil().getMessageConfig().reload();
                        p.sendMessage(Utility.getMsg(p, "reload"));
                        return true;
                    case "template":
                        if (args.length > 2) {
                            String name = args[2].toLowerCase();
                            File file = new File(HLootchest.getFilePath()+ "/v1_8_R3/templates", name + ".yml");
                            switch (args[1].toLowerCase()) {
                                case "delete":
                                    boolean isDeleted = file.delete();
                                    if (!isDeleted) {
                                        p.sendMessage(Utility.getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "select":
                                    if (file.exists()) {
                                        HLootchest.getAPI().getConfigUtil().getMainConfig().set("template", name);
                                        HLootchest.templateConfig = new ConfigManager(HLootchest.getInstance(), HLootchest.getAPI().getConfigUtil().getMainConfig().getString("template"), HLootchest.getFilePath() + "/v1_8_R3/templates");
                                        p.sendMessage(Utility.getMsg(p, "templateSelected").replace("{template}", name));
                                        return true;
                                    } else {
                                        p.sendMessage(Utility.getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                default:
                                    p.sendMessage(ChatColor.GREEN + "Available actions:");
                                    p.sendMessage("- delete");
                                    p.sendMessage("- select");
                            }
                        } else {
                            p.sendMessage(Utility.c("&cPlease enter template name!"));
                        }
                    case "add":
                        if (args.length > 3) {
                            Player player = Bukkit.getPlayer(args[1]);
                            if (player == null) {
                                p.sendMessage(Utility.getMsg(p, "invalidArguments"));
                                return true;
                            }
                            if (!HLootchest.getNms().getRegistrationData().containsKey(args[2])) {
                                p.sendMessage(Utility.getMsg(p, "lootchestNotFound"));
                                return true;
                            }
                            int amount = Integer.valueOf(args[3]);
                            if (amount == 0) {
                                p.sendMessage(Utility.getMsg(p, "invalidArguments"));
                                return true;
                            }
                            boolean completed = HLootchest.getAPI().getDatabaseUtil().getDb().addData(player, args[2], amount, "lootchests");
                            if (!completed) {
                                p.sendMessage(Utility.getMsg(p, "invalidArguments"));
                                return true;
                            } else {
                                if (amount > 0) {
                                    p.sendMessage(Utility.getMsg(p, "addLootChest")
                                            .replace("{amount}", String.valueOf(amount))
                                            .replace("{player}", player.getName())
                                            .replace("{type}", args[2]));
                                    return true;
                                } else {
                                    p.sendMessage(Utility.getMsg(p, "removeLootChest")
                                            .replace("{amount}", String.valueOf(amount))
                                            .replace("{player}", player.getName())
                                            .replace("{type}", args[2]));
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage(Utility.getMsg(p, "invalidArguments"));
                            return true;
                        }
                    case "templateslist":
                        try {
                            File[] files = new File((HLootchest.getFilePath() + "/v1_8_R3/templates")).listFiles();
                            p.sendMessage(ChatColor.GREEN + "Available templates:");
                            for (File file : files) {
                                p.sendMessage("- " + file.getName().replace(".yml", ""));
                            }
                            return true;
                        } catch (NullPointerException ex) {
                            throw new NullPointerException(ex.getMessage());
                        }
                    default:
                        Utility.sendHelp(p, "admin");
                        return true;
                }
            }
        }
        return true;
    }
}

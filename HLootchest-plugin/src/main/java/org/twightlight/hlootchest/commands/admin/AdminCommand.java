package org.twightlight.hlootchest.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.menus.LCSMainMenu;
import org.twightlight.hlootchest.setup.menus.TSMainMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;
import java.util.ArrayList;

public class AdminCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utility.info("This command must be executed by a player!");
        }
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
                        HLootchest.getAPI().getConfigUtil().getMainConfig().reload();
                        p.sendMessage(Utility.getMsg(p, "reload"));
                        return true;
                    case "template":
                        if (args.length > 2) {
                            String name = args[2].toLowerCase();
                            File file = new File(HLootchest.getFilePath()+ "/templates", name + ".yml");
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
                                        HLootchest.templateConfig = new ConfigManager(HLootchest.getInstance(), HLootchest.getAPI().getConfigUtil().getMainConfig().getString("template"), HLootchest.getFilePath() + "/templates");
                                        p.sendMessage(Utility.getMsg(p, "templateSelected").replace("{template}", name));
                                        return true;
                                    } else {
                                        p.sendMessage(Utility.getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "edit":
                                    if (file.exists()) {
                                        TConfigManager conf = new ConfigManager(HLootchest.getInstance(), name, HLootchest.getFilePath() + "/templates");
                                        SetupSessions session = null;
                                        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
                                            session = new SetupSessions(p, conf);
                                        } else {
                                            if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof SetupSessions) {
                                                session = (SetupSessions) HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
                                            }
                                        }
                                        if (session != null && session.getConfigManager().getYml().getName().equals(conf.getYml().getName())) {
                                            if (session.getInvConstructor() != null) {
                                                session.getInvConstructor().createNew();
                                            } else {
                                                new TSMainMenu(p, conf);
                                                p.getInventory().addItem(HLootchest.getNms().createItem(Material.DIAMOND, "", 0, "&bSetup Item", new ArrayList<>(), false));

                                            }
                                        }
                                        return true;
                                    } else {
                                        p.sendMessage(Utility.getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "create":
                                    if (file.exists()) {
                                        p.sendMessage(Utility.getMsg(p, "templateExist"));
                                        return true;
                                    } else {
                                        new ConfigManager(HLootchest.getInstance(), name, HLootchest.getFilePath() + "/templates");
                                        p.sendMessage(Utility.getMsg(p, "createTemplate").replace("{template}", name));
                                        return true;
                                    }
                                default:
                                    p.sendMessage(ChatColor.GREEN + "Available actions:");
                                    p.sendMessage("- create");
                                    p.sendMessage("- delete");
                                    p.sendMessage("- edit");
                                    p.sendMessage("- select");
                                    return true;

                            }
                        } else {
                            p.sendMessage(Utility.getMsg(p, "invalidArguments"));
                            return true;
                        }
                    case "lootchestssetup":
                        SetupSessions session = null;
                        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
                            session = new SetupSessions(p, HLootchest.getAPI().getConfigUtil().getBoxesConfig());
                        } else {
                            if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof SetupSessions) {
                                session = (SetupSessions) HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
                            }
                        }
                        if (session != null) {
                            if (session.getInvConstructor() != null) {
                                session.getInvConstructor().createNew();
                            } else {
                                new LCSMainMenu(p, HLootchest.getAPI().getConfigUtil().getBoxesConfig());
                                p.getInventory().addItem(HLootchest.getNms().createItem(Material.DIAMOND, "", 0, "&bSetup Item", new ArrayList<>(), false));

                            }
                        }
                        return true;
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
                            File[] files = new File((HLootchest.getFilePath() + "/templates")).listFiles();
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

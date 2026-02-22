package org.twightlight.hlootchest.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.config.YamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.LootChestBrowseMenu;
import org.twightlight.hlootchest.setup.TemplateBrowseMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class AdminCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utility.info("This command must be executed by a player!");
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("hlc.admin")) {
                p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "noPerms"));
            }
            if (args.length < 1) {
                HLootChest.getAPI().getLanguageUtil().sendHelp(p, "admin");
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        HLootChest.getAPI().getConfigUtil().getTemplateConfig().reload();

                        Set<String> types = HLootChest.getAPI().getConfigUtil().getBoxesConfigs().keySet();
                        for (String type : types) {
                            HLootChest.getAPI().getConfigUtil().getBoxesConfig(type).reload();
                        }
                        HLootChest.getAPI().getConfigUtil().getMessageConfig().reload();
                        HLootChest.getAPI().getConfigUtil().getMainConfig().reload();
                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "reload"));
                        return true;
                    case "template":
                        if (args.length > 2) {
                            String name = args[2].toLowerCase();
                            File file = new File(HLootChest.getFilePath()+ "/templates", name + ".yml");
                            switch (args[1].toLowerCase()) {
                                case "delete":
                                    boolean isDeleted = file.delete();
                                    if (!isDeleted) {
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "select":
                                    if (file.exists()) {
                                        HLootChest.getAPI().getConfigUtil().getMainConfig().set("template", name);
                                        ConfigManager.templateConfig = new YamlWrapper(HLootChest.getInstance(), HLootChest.getAPI().getConfigUtil().getMainConfig().getString("template"), HLootChest.getFilePath() + "/templates");
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "templateSelected").replace("{template}", name));
                                        return true;
                                    } else {
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "edit":
                                    if (file.exists()) {
                                        TYamlWrapper conf = new YamlWrapper(HLootChest.getInstance(), name, HLootChest.getFilePath() + "/templates");
                                        SetupSession session = null;
                                        if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
                                            session = new SetupSession(p, conf);
                                        } else {
                                            if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof SetupSession) {
                                                session = (SetupSession) HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p);
                                            }
                                        }
                                        if (session != null && session.getConfigManager().getYml().getName().equals(conf.getYml().getName())) {
                                            if (session.getInvConstructor() != null) {
                                                session.getInvConstructor().createNew();
                                            } else {
                                                new TemplateBrowseMenu(p, conf);
                                                p.getInventory().addItem(HLootChest.getNms().createItem(Material.DIAMOND, "", 0, "&bSetup Item", new ArrayList<>(), false));

                                            }
                                        }
                                        return true;
                                    } else {
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "templateNotFound"));
                                        return true;
                                    }
                                case "create":
                                    if (file.exists()) {
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "templateExist"));
                                        return true;
                                    } else {
                                        new YamlWrapper(HLootChest.getInstance(), name, HLootChest.getFilePath() + "/templates");
                                        p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "createTemplate").replace("{template}", name));
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
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "invalidArguments"));
                            return true;
                        }
                    case "lootchestssetup":
                        SetupSession session = null;
                        if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
                            session = new SetupSession(p, null);
                        } else {
                            if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof SetupSession) {
                                session = (SetupSession) HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p);
                            }
                        }
                        if (session != null) {
                            if (session.getInvConstructor() != null) {
                                session.getInvConstructor().createNew();
                            } else {
                                new LootChestBrowseMenu(p);
                                p.getInventory().addItem(HLootChest.getNms().createItem(Material.DIAMOND, "", 0, "&bSetup Item", new ArrayList<>(), false));

                            }
                        }
                        return true;
                    case "add":
                    case "give":
                        if (args.length > 3) {
                            Player player = Bukkit.getPlayer(args[1]);
                            if (player == null) {
                                p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "invalidArguments"));
                                return true;
                            }
                            if (!HLootChest.getNms().getRegistrationData().containsKey(args[2])) {
                                p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "lootchestNotFound"));
                                return true;
                            }
                            int amount = Integer.valueOf(args[3]);
                            if (amount == 0) {
                                p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "invalidArguments"));
                                return true;
                            }
                            boolean completed = HLootChest.getAPI().getDatabaseUtil().getDatabase().addLootchest(player, args[2], amount, "lootchests");
                            if (!completed) {
                                p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "invalidArguments"));
                                return true;
                            } else {
                                if (amount > 0) {
                                    p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "addLootChest")
                                            .replace("{amount}", String.valueOf(amount))
                                            .replace("{player}", player.getName())
                                            .replace("{type}", args[2]));
                                    return true;
                                } else {
                                    p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "removeLootChest")
                                            .replace("{amount}", String.valueOf(amount))
                                            .replace("{player}", player.getName())
                                            .replace("{type}", args[2]));
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage(HLootChest.getAPI().getLanguageUtil().getMsg(p, "invalidArguments"));
                            return true;
                        }
                    case "templateslist":
                        try {
                            File[] files = new File((HLootChest.getFilePath() + "/templates")).listFiles();
                            p.sendMessage(ChatColor.GREEN + "Available templates:");
                            for (File file : files) {
                                p.sendMessage("- " + file.getName().replace(".yml", ""));
                            }
                            return true;
                        } catch (NullPointerException ex) {
                            throw new NullPointerException(ex.getMessage());
                        }
                    case "clean-debug":
                        if (HLootChest.getAPI().getDebugService().isDebug()) {
                            Utility.clean(p.getLocation().getChunk(), "LootchestVehicle", HLootChest.getScheduler());
                            Utility.clean(p.getLocation().getChunk(), "removeOnRestart", HLootChest.getScheduler());
                        } else {
                            p.sendMessage(ChatColor.RED + "Not in debug mode!");
                        }
                        return true;
                    default:
                        HLootChest.getAPI().getLanguageUtil().sendHelp(p, "admin");
                        return true;
                }
            }
        }
        return true;
    }
}

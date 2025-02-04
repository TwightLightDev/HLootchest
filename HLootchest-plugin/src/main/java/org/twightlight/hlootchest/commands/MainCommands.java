package org.twightlight.hlootchest.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.sessions.LootChestSessions;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;



public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                new LootChestSessions(p, "regular");
            } else {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        HLootchest.getAPI().getConfigUtil().getTemplateConfig().reload();
                        HLootchest.getAPI().getConfigUtil().getBoxesConfig().reload();
                        p.sendMessage(Utility.c("&aHlootchest has been reloaded!"));
                        return true;
                    case "template":
                        if (args.length > 2) {
                            String name = args[2].toLowerCase();
                            File file = new File(HLootchest.getFilePath()+ "/templates", name + ".yml");
                            switch (args[1].toLowerCase()) {
                                case "delete":
                                    boolean isDeleted = file.delete();
                                    if (!isDeleted) {
                                        p.sendMessage(ChatColor.RED + "Template not found!");
                                    }
                                case "select":
                                    if (file.exists()) {
                                        HLootchest.getAPI().getConfigUtil().getMainConfig().set("template", name);
                                        HLootchest.templateConfig = new ConfigManager(HLootchest.getInstance(), HLootchest.getAPI().getConfigUtil().getMainConfig().getString("template"), HLootchest.getFilePath() + "/templates");
                                        p.sendMessage(Utility.c("&aSucessfully set template to ") + ChatColor.WHITE + name + Utility.c("&a!"));
                                    } else {
                                        p.sendMessage(Utility.c("&cTemplate ") + ChatColor.WHITE + name + Utility.c(" &cnot found!"));
                                    }
                            }
                        } else {
                            p.sendMessage(Utility.c("&cPlease enter template name"));
                        }
                    case "leave":
                        TBox box = HLootchest.getNms().getBoxFromPlayer(p);
                        if (HLootchest.getNms().getBoxFromPlayer(p) != null) {
                            box.removeVehicle(p);
                            box.getOwner().teleport(box.getPlayerInitialLoc());
                            box.remove();
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                if (!online.equals(p)) {
                                    online.showPlayer(p);
                                }
                            }
                            HLootchest.getNms().removeButtonsFromPlayer(p, ButtonType.FUNCTIONAL);
                            HLootchest.getNms().removeButtonsFromPlayer(p, ButtonType.REWARD);
                        }

                }
            }
        }
        return true;
    }


}



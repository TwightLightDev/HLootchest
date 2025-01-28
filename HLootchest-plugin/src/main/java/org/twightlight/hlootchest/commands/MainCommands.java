package org.twightlight.hlootchest.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.sessions.TSessionManager;
import org.twightlight.hlootchest.config.configs.TemplateConfig;
import org.twightlight.hlootchest.sessions.setup.SetupSession;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;


public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                String identifier = "regular";
                TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
                ItemStack helm = Utility.createItem(Material.valueOf(conf.getString(identifier+".icon.material")), conf.getString(identifier+".icon.head_value"), conf.getInt(identifier+".icon.data"), "", new ArrayList<>(), false);
                HLootchest.getNms().spawnBox(identifier, p, p.getLocation().add(0, 1.2, 0), helm);
            } else {
                switch (args[0].toLowerCase()) {
                    case "removeall":
                        try {
                            if (HLootchest.getNms().getGlobalButtons().get(p) == null) {
                                return true;
                            }
                            int times = HLootchest.getNms().getGlobalButtons().get(p).size();
                            for (int i = 0; i < times; i++) {
                                HLootchest.getNms().getGlobalButtons().get(p).get(0).remove();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    case "reload":
                        return true;
                    case "template":
                        TSessionManager session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
                        switch (args[1].toLowerCase()) {
                            case "setup":
                                String name = args[2].toLowerCase();
                                TConfigManager conf = new TemplateConfig(HLootchest.getInstance(), name, HLootchest.getFilePath() + "/templates");
                                p.sendMessage("Start setup your template: " + name);
                                new SetupSession(p, conf, name);
                                return true;
                            case "input":
                                if (session instanceof SetupSession) {
                                    SetupSession session1 = (SetupSession) session;
                                    session1.newTypeChatSession(args[2].toLowerCase());
                                    p.sendMessage(Utility.c("&aYou are modifying the value of: " + ChatColor.WHITE + args[2].toLowerCase()));
                                    p.sendMessage(Utility.c("&aType 'cancel' to cancel this action!"));
                                } else {
                                    p.sendMessage(Utility.c("&cInvalid Session!"));
                                }
                                return true;
                            case "setup close":
                                if (session != null) {
                                    session.close();
                                } else {
                                    p.sendMessage(Utility.c("&cInvalid Session!"));
                                }
                                return true;
                            case "save":
                                if (session instanceof SetupSession) {
                                    SetupSession session1 = (SetupSession) session;
                                    session1.save();
                                    p.sendMessage(Utility.c("&aYou saved the template!"));
                                } else {
                                    p.sendMessage(Utility.c("&cInvalid Session!"));
                                }
                                return true;
                            default:
                                p.sendMessage(Utility.c("&cInvalid argument!"));
                        }
                        return true;
                }
            }
        }
        return true;
    }
}

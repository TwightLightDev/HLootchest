package org.twightlight.hlootchest.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;


public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                if (HLootchest.getNms().getBoxFromPlayer(p) == null) {
                    String identifier = "regular";
                    TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
                    ItemStack icon = Utility.createItem(Material.valueOf(conf.getString(identifier + ".icon.material")), conf.getString(identifier + ".icon.head_value"), conf.getInt(identifier + ".icon.data"), "", new ArrayList<>(), false);
                    TConfigManager templateconfig = HLootchest.getAPI().getConfigUtil().getTemplateConfig();
                    HLootchest.getNms().spawnBox(identifier, p, icon, templateconfig);

                    if (templateconfig.getYml().getConfigurationSection(identifier + ".buttons") != null) {
                        Set<String> buttons = templateconfig.getYml().getConfigurationSection(identifier + ".buttons").getKeys(false);
                        for (String button : buttons) {
                            String path = identifier + ".buttons" + "." + button;
                            ItemStack buttonicon = Utility.createItem(Material.valueOf(templateconfig.getString(path + ".icon.material")), templateconfig.getString(path + ".icon.head_value"), templateconfig.getInt(path + ".icon.data"), "", new ArrayList<>(), false);
                            HLootchest.getNms().spawnButton(ButtonType.FUNCTIONAL, p, buttonicon, path, templateconfig);
                        }
                    }
                }
            } else {
                switch (args[0].toLowerCase()) {
                    case "deletebox":
                        if (HLootchest.getNms().getBoxFromPlayer(p) != null) {
                            HLootchest.getNms().getBoxFromPlayer(p).remove();
                            HLootchest.getNms().removeButtonsFromPlayer(p, ButtonType.FUNCTIONAL);
                        }
                    case "reload":
                        HLootchest.getAPI().getConfigUtil().getTemplateConfig().reload();
                        HLootchest.getAPI().getConfigUtil().getBoxesConfig().reload();
                        return true;
                    case "template":
                        String name = args[2].toLowerCase();
                        switch (args[1].toLowerCase()) {
                            case "delete":
                                File file = new File(HLootchest.getFilePath()+ "/resources/templates", name + ".yml");
                                boolean isDeleted = file.delete();
                                if (!isDeleted) {
                                    p.sendMessage(ChatColor.RED + "Template not found!");
                                }
                        }
                }
            }
        }
        return true;
    }
}

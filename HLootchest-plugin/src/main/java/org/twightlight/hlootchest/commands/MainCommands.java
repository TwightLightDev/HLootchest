package org.twightlight.hlootchest.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.BoxType;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;


public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                String identifier = BoxType.REGULAR.getIdentifier();
                TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
                ItemStack helm = Utility.createItem(Material.valueOf(conf.getString(identifier+".icon.material")), conf.getString(identifier+".icon.head_value"), conf.getInt(identifier+".icon.data"), "", new ArrayList<>(), false);
                HLootchest.getNms().spawnBox(p, p.getLocation().add(0, 1.2, 0), BoxType.REGULAR, helm);
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
                    case "checkdata":
                        p.sendMessage(HLootchest.getNms().getGlobalButtons().get(p).toString());
                        return true;
                    case "reload":

                        return true;
                }
            }
        }
        return true;
    }
}

package org.twightlight.hlootchest.objects;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.Main;
import org.twightlight.hlootchest.utils.Utility;

import java.util.List;

public class Reward {

    private final List<String> actions;

    public Reward(List<String> actions) {
        this.actions = actions;
    }

    public void accept(Player p) {
        for (String action : actions) {
            String[] dataset = action.split(" ", 2);
            if (dataset[0].equals("[player]")) {
                p.performCommand(HLootchest.getAPI().getLanguageUtil().replaceCommand(p, dataset[1]));
            } else if (dataset[0].equals("[console]")) {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.getServer().dispatchCommand(console, HLootchest.getAPI().getLanguageUtil().replaceCommand(p, dataset[1]));
            } else if ((dataset[0].equals("[xp_set]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setXp(p, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setXp(p, amount);
                }
            } else if ((dataset[0].equals("[xp_add]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addXp(p, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addXp(p, amount);
                }
            } else if ((dataset[0].equals("[level_set]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setLevel(p, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setLevel(p, amount);
                }
            } else if ((dataset[0].equals("[level_add]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addLevel(p, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addLevel(p, amount);
                }
            }
        }
    }
}

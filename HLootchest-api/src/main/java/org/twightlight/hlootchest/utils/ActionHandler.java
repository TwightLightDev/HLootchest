package org.twightlight.hlootchest.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;

public class ActionHandler {

    private HLootchest api;

    public ActionHandler(HLootchest api) {
        this.api = api;
    }


    public void handle(String action, Player p, ButtonType type) {
        String[] dataset = action.split(" ", 2);

        if (dataset[0].equals("[player]")) {
            p.performCommand(api.getLanguageUtil().replaceCommand(p, dataset[1]));
        } else if (dataset[0].equals("[console]")) {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.getServer().dispatchCommand(console, api.getLanguageUtil().replaceCommand(p, dataset[1]));
        } else if (dataset[0].equals("[message]")) {
            p.sendMessage(
                    ChatColor.translateAlternateColorCodes('&'
                            , PlaceholderAPI.setPlaceholders(p
                                    , dataset[1])));
        } else if ((dataset[0].equals("[open]"))) {
            if (type == ButtonType.FUNCTIONAL) {
                api.getNMS().getBoxFromPlayer(p).open();
            }
        } else if ((dataset[0].equals("[close]"))) {
            if (type == ButtonType.FUNCTIONAL) {
                api.getSessionUtil().getSessionFromPlayer(p).close();
            }
        } else if ((dataset[0].equals("[xp_set]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setXp(p, amount);
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setXp(p, amount);
            }
        } else if ((dataset[0].equals("[xp_add]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addXp(p, amount);
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addXp(p, amount);
            }
        } else if ((dataset[0].equals("[level_set]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setLevel(p, amount);
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setLevel(p, amount);
            }
        } else if ((dataset[0].equals("[level_add]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addLevel(p, amount);
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try {
                    amount = Integer.parseInt(dataset[1]);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
                api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addLevel(p, amount);
            }
        }
    }
}

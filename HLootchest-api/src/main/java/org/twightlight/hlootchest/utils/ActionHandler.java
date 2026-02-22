package org.twightlight.hlootchest.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.scheduler.SchedulerAdapter;

public class ActionHandler {

    private final HLootchest api;
    private final SchedulerAdapter scheduler;

    public ActionHandler(HLootchest api, SchedulerAdapter scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }

    public void handle(String action, Player p, ButtonType type) {
        String[] dataset = action.split(" ", 2);
        if (dataset[0].equals("[player]")) {
            scheduler.runTask(p, () ->
                    p.performCommand(api.getLanguageUtil().replaceCommand(p, dataset[1])));
        } else if (dataset[0].equals("[console]")) {
            scheduler.runTask(p, () -> {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.getServer().dispatchCommand(console, api.getLanguageUtil().replaceCommand(p, dataset[1]));
            });
        } else if (dataset[0].equals("[message]")) {
            p.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            PlaceholderAPI.setPlaceholders(p, dataset[1])));
        } else if ((dataset[0].equals("[open]"))) {
            if (type == ButtonType.FUNCTIONAL) {
                scheduler.runTask(p, () -> api.getNMS().getBoxFromPlayer(p).open());
            }
        } else if ((dataset[0].equals("[close]"))) {
            if (type == ButtonType.FUNCTIONAL) {
                scheduler.runTask(p, () -> api.getSessionUtil().getSessionFromPlayer(p).close());
            }
        } else if ((dataset[0].equals("[xp_set]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setXp(p, finalAmount));
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setXp(p, finalAmount));
            }
        } else if ((dataset[0].equals("[xp_add]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addXp(p, finalAmount));
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addXp(p, finalAmount));
            }
        } else if ((dataset[0].equals("[level_set]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setLevel(p, finalAmount));
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setLevel(p, finalAmount));
            }
        } else if ((dataset[0].equals("[level_add]"))) {
            if (api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addLevel(p, finalAmount));
            } else if (api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                int amount;
                try { amount = Integer.parseInt(dataset[1]); } catch (NumberFormatException e) { amount = 0; }
                int finalAmount = amount;
                scheduler.runTask(p, () ->
                        api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addLevel(p, finalAmount));
            }
        }
    }
}

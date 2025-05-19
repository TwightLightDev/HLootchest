
package org.twightlight.hlootchest.supports.protocol.v1_19_R3.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.twightlight.hlootchest.api.events.player.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.buttons.Button;

import java.util.List;

public class ClickEvent implements Listener {
    NMSHandler nms = Main.handler;

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            TBox box;

            Player player = event.getPlayer();

            ArmorStand armorStand = (ArmorStand) event.getRightClicked();

            int entityId = armorStand.getEntityId();
            TButton button = this.nms.getButtonFromId(entityId);
            if (button != null && button.getOwner() == player && button.isClickable()) {
                handleButtonInteraction(player, button);
                event.setCancelled(true);
            }
            if ((box = BoxManager.boxlists.get(entityId)) != null && box.getOwner() == player && box.isClickable() && box.isClickToOpen()) {
                handleButtonInteraction(box);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand && event.getDamager() instanceof Player) {
            TBox box;

            Player player = (Player) event.getDamager();

            ArmorStand armorStand = (ArmorStand) event.getEntity();

            int entityId = armorStand.getEntityId();
            TButton button = this.nms.getButtonFromId(entityId);
            if (button != null && button.getOwner() == player && button.isClickable()) {
                handleButtonInteraction(player, button);
                event.setCancelled(true);
            }
            if ((box = BoxManager.boxlists.get(entityId)) != null && box.getOwner() == player && box.isClickable() && box.isClickToOpen()) {
                handleButtonInteraction(box);
                event.setCancelled(true);
            }
        }
    }

    private void handleButtonInteraction(Player player, TButton button) {
        PlayerButtonClickEvent event = new PlayerButtonClickEvent(player, button);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || button.isHiding() || button.isPreview()) {
            return;
        }
        if (((Button)button).getSound() != null) {
            Main.handler.playSound(player, player.getLocation(), ((Button)button).getSound().getSoundString(), ((Button)button).getSound().getYaw(), ((Button)button).getSound().getPitch());
        }
        List<String> actions = button.getActions();
        for (String stringAction : actions) {
            String[] dataset = stringAction.split(" ", 2);
            if (dataset[0].equals("[player]")) {
                player.performCommand(Main.api.getLanguageUtil().replaceCommand(player, dataset[1]));
            } else if (dataset[0].equals("[console]")) {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.getServer().dispatchCommand(console, Main.api.getLanguageUtil().replaceCommand(player, dataset[1]));
            } else if (dataset[0].equals("[message]")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, (String)dataset[1])));
            } else if (dataset[0].equals("[open]")) {
                Main.handler.getBoxFromPlayer(player).open();
            } else if ((dataset[0].equals("[close]"))) {
                Main.api.getSessionUtil().getSessionFromPlayer(player).close();
            } else if ((dataset[0].equals("[xp_set]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setXp(player, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setXp(player, amount);
                }
            } else if ((dataset[0].equals("[xp_add]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addXp(player, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addXp(player, amount);
                }
            } else if ((dataset[0].equals("[level_set]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().setLevel(player, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().setLevel(player, amount);
                }
            } else if ((dataset[0].equals("[level_add]"))) {
                if (Main.api.getHooksLoader().getBedWars1058Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars1058Hook().getBedWarsService().getLevelsUtil().addLevel(player, amount);
                } else if (Main.api.getHooksLoader().getBedWars2023Hook().hasBedWars()) {
                    int amount;
                    try {
                        amount = Integer.parseInt(dataset[1]);
                    } catch (NumberFormatException e) {
                        amount = 0;
                    }
                    Main.api.getHooksLoader().getBedWars2023Hook().getBedWarsService().getLevelsUtil().addLevel(player, amount);
                }
            }
        }

    }

    private void handleButtonInteraction(TBox box) {
        if (!box.isClickToOpen()) {
            return;
        }
            box.open();
    }
}

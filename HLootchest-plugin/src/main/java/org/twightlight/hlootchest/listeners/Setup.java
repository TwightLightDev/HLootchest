package org.twightlight.hlootchest.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.TSessions;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.setup.MenuManager;

public class Setup implements Listener {
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(((Player) e.getWhoClicked())) != null) {
            e.setCancelled(true);
            TSessions session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(((Player) e.getWhoClicked()));
            if (session instanceof SetupSessions) {
                Player p = (Player) e.getWhoClicked();
                Executable action = MenuManager.getButtonsList().get(p.getUniqueId()).get(e.getCurrentItem());
                if (action != null) {
                    action.execute(e);
                }
            }
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatSessions session = ChatSessions.getSession(player);

        if (session != null) {
            event.setCancelled(true);
            session.handleInput(event.getMessage());
        }
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(e.getPlayer()) != null &&
                HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(e.getPlayer()) instanceof SetupSessions) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getPlayer().getItemInHand().getType() == Material.DIAMOND && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Setup Item")) {
                    e.setCancelled(true);
                    SetupSessions session = (SetupSessions) HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(e.getPlayer());
                    session.getInvConstructor().createNew();
                }
            }
        }
    }
}

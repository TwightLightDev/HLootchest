package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.functionals.ClickableButtons;
import org.twightlight.hlootchest.setup.menus.MenuManager;

public class Setup implements Listener {
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(((Player) e.getWhoClicked())) != null) {
            e.setCancelled(true);
            TSessions session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(((Player) e.getWhoClicked()));
            if (session instanceof SetupSessions) {
                Player p = (Player) e.getWhoClicked();
                ClickableButtons action = MenuManager.getButtonsList().get(p.getUniqueId()).get(e.getCurrentItem());
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
}

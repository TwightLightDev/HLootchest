package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.sessions.TSessionManager;
import org.twightlight.hlootchest.sessions.setup.SetupSession;

public class PlayerChat implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        TSessionManager session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        if (session instanceof SetupSession) {
            SetupSession session1 = (SetupSession) session;
            if (session1.isInTypeChat()) {
                e.setCancelled(true);
                if (e.getMessage().equals("cancel")) {
                    session1.closeTypeChatSession(e.getMessage());

                } else {
                    session1.closeTypeChatSession(null);
                }
            }
        }
    }
}

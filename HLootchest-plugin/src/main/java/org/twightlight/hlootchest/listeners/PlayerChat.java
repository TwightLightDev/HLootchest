package org.twightlight.hlootchest.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.sessions.TSessionManager;
import org.twightlight.hlootchest.sessions.setup.SetupSession;
import org.twightlight.hlootchest.utils.Utility;

public class PlayerChat implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        TSessionManager session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        if (session instanceof SetupSession) {
            SetupSession session1 = (SetupSession) session;
            if (session1.isInTypeChat()) {
                e.setCancelled(true);
                if (!e.getMessage().equals("cancel")) {
                    p.sendMessage(Utility.c("&aSuccessfully modified input '") + ChatColor.WHITE + session1.getInputPath() + Utility.c("'&a to ") + ChatColor.WHITE + e.getMessage());
                    session1.closeTypeChatSession(e.getMessage());
                } else {
                    p.sendMessage(Utility.c("&aYou cancelled your action!"));
                    session1.closeTypeChatSession(null);
                }
            }
        }
    }
}

package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.twightlight.hlootchest.HLootChest;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
            HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p).close();
        }
    }
}

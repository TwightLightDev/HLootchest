package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.hlootchest.HLootchest;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (HLootchest.getNms().getBoxFromPlayer(p) != null) {
            HLootchest.getNms().getBoxFromPlayer(p).removeVehicle(p);
            HLootchest.getNms().getBoxFromPlayer(p).remove();
            if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }
}

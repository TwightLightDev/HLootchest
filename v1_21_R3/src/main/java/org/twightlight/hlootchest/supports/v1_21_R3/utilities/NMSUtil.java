package org.twightlight.hlootchest.supports.v1_21_R3.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

public class NMSUtil extends org.twightlight.hlootchest.supports.v1_19_R3.utilities.NMSUtil {
    @Override
    public void lockAngle(Player p, Location loc, long duration) {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > duration * 50)
                    cancel();
                if (p.getLocation().getYaw() != loc.getYaw() || p.getLocation().getPitch() != loc.getPitch()) {
                    if (org.twightlight.hlootchest.supports.v1_21_R3.Main.hasProtocolLib()) {
                        org.twightlight.hlootchest.supports.v1_21_R3.Main.getProtocolService().setPlayerLocation(p, loc);
                    }
                    Main.api.getDebugService().sendDebugMsg(p, "Attempt to teleport " + p + "to " + loc);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 2L);
    }
}

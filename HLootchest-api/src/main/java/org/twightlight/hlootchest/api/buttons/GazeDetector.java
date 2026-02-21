package org.twightlight.hlootchest.api.buttons;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class GazeDetector {

    public static boolean isLooking(Player player, double targetX, double targetY, double targetZ, boolean modern) {
        if (modern) {
            double maxDistance = 5.0;
            Location eyeLoc = player.getEyeLocation();
            Vector rayOrigin = eyeLoc.toVector();
            Vector rayDir = eyeLoc.getDirection().normalize();

            Vector min = new Vector(targetX - 0.3, targetY + 1.6 - 0.1875, targetZ - 0.3);
            Vector max = new Vector(targetX + 0.3, targetY + 2.05, targetZ + 0.3);

            return rayIntersectsAABB(rayOrigin, rayDir, min, max, maxDistance);
        } else {
            Location eyeLoc = player.getEyeLocation();
            Vector dir = eyeLoc.getDirection().normalize();
            Vector pos = eyeLoc.toVector();
            Vector target = new Vector(targetX, targetY + 1.6, targetZ);
            Vector toTarget = target.subtract(pos).normalize();
            return dir.dot(toTarget) > 0.98;
        }
    }

    private static boolean rayIntersectsAABB(Vector origin, Vector dir, Vector min, Vector max, double maxDist) {
        double tMin = 0.0;
        double tMax = maxDist;

        double[] o = {origin.getX(), origin.getY(), origin.getZ()};
        double[] d = {dir.getX(), dir.getY(), dir.getZ()};
        double[] mn = {min.getX(), min.getY(), min.getZ()};
        double[] mx = {max.getX(), max.getY(), max.getZ()};

        for (int i = 0; i < 3; i++) {

            double d1 = d[i];
            if (d[i] == 0) {
                d1 = 0.001;
            }
            double invD = 1.0 / d1;
            double t0 = (mn[i] - o[i]) * invD;
            double t1 = (mx[i] - o[i]) * invD;
            if (invD < 0.0) { double tmp = t0; t0 = t1; t1 = tmp; }
            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);
            if (tMax < tMin) return false;
        }
        return true;
    }
}


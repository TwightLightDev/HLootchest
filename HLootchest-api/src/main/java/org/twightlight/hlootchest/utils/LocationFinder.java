package org.twightlight.hlootchest.utils;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationFinder {

    public static List<Location> findSafeLocations(Location loc, int maxLocations,int INNER_RADIUS, int OUTER_RADIUS) {
        List<Location> safeLocations = new ArrayList<>();
        World world = loc.getWorld();

        for (int x = -OUTER_RADIUS; x <= OUTER_RADIUS; x++) {
            for (int z = -OUTER_RADIUS; z <= OUTER_RADIUS; z++) {
                double distance = Math.sqrt(x * x + z * z);

                if (distance < INNER_RADIUS || distance > OUTER_RADIUS) {
                    continue;
                }

                Location checkLoc = loc.clone().add(x, 0, z);
                Location groundLoc = getHighestNonAirBlock(checkLoc);
                if (groundLoc == null) continue;

                Location airLoc = groundLoc.clone().add(0, 1, 0);
                if (world.getBlockAt(airLoc).getType() == XMaterial.AIR.get()) {
                    safeLocations.add(airLoc);
                }
            }
        }

        Collections.shuffle(safeLocations);
        return safeLocations.subList(0, Math.min(maxLocations, safeLocations.size()));
    }

    private static Location getHighestNonAirBlock(Location loc) {
        World world = loc.getWorld();
        int y = world.getHighestBlockYAt(loc);

        if (y <= 0) return null;

        for (int i = 0; i < 5; i++) {
            Location groundLoc = new Location(world, loc.getX(), y - i, loc.getZ());
            Material groundType = world.getBlockAt(groundLoc).getType();

            if (groundType.isSolid() && groundType != XMaterial.WATER.get() && groundType != XMaterial.LAVA.get()) {
                return groundLoc;
            }
        }
        return null;
    }
}

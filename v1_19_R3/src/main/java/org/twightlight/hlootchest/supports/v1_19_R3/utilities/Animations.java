package org.twightlight.hlootchest.supports.v1_19_R3.utilities;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Animations {
    public static void drawCircle(Player player, ArmorStand packetArmorStand, Location center, double radius, double rotX, double rotY, double rotZ, int points) {
        double radiansRotX = Math.toRadians(rotX);
        double radiansRotY = Math.toRadians(rotY);
        double radiansRotZ = Math.toRadians(rotZ);

        for (int i = 0; i < points; ++i) {
            double angle = Math.PI * 2 * (double) i / (double) points;
            double x = radius * Math.cos(angle);
            double y = 0.0;
            double z = radius * Math.sin(angle);

            double tempY = y * Math.cos(radiansRotX) - z * Math.sin(radiansRotX);
            double tempZ = y * Math.sin(radiansRotX) + z * Math.cos(radiansRotX);
            y = tempY;
            z = tempZ;

            double tempX = x * Math.cos(radiansRotY) + z * Math.sin(radiansRotY);
            tempZ = -x * Math.sin(radiansRotY) + z * Math.cos(radiansRotY);
            x = tempX;
            z = tempZ;

            tempX = x * Math.cos(radiansRotZ) - y * Math.sin(radiansRotZ);
            tempY = x * Math.sin(radiansRotZ) + y * Math.cos(radiansRotZ);
            x = tempX;
            y = tempY;

            Location newLoc = center.clone().add(x, y, z);

            Vector direction = newLoc.toVector().subtract(packetArmorStand.getLocation().toVector());
            packetArmorStand.teleport(packetArmorStand.getLocation().add(direction.multiply(0.1)));
        }
    }

    public static void moveBackward(Player player, ArmorStand armorStand, float val) {
        Location loc = armorStand.getLocation();
        float yaw = loc.getYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        loc.add(-dx, 0, -dz);
        armorStand.teleport(loc);
    }

    public static void moveForward(Player player, ArmorStand armorStand, float val) {
        Location loc = armorStand.getLocation();
        float yaw = loc.getYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        loc.add(dx, 0, dz);
        armorStand.teleport(loc);
    }


    public static void moveUp(Player player, ArmorStand armorStand, double value) {
        armorStand.teleport(armorStand.getLocation().add(0, value, 0));
    }

    public static void spin(Player player, ArmorStand armorStand, float val) {
        while (val > 180.0F)
            val -= 360.0F;
        while (val < -180.0F)
            val += 360.0F;
        Location loc = armorStand.getLocation();
        loc.setYaw(val);
        armorStand.teleport(loc);
    }

}

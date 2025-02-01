package org.twightlight.hlootchest.supports.v1_8_R3.animations;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class DrawCircle {
    public DrawCircle(Player player, EntityArmorStand packetArmorStand, Location center, double radius, int points, double rotX, double rotY, double rotZ) {

        double radiansRotX = Math.toRadians(rotX);
        double radiansRotY = Math.toRadians(rotY);
        double radiansRotZ = Math.toRadians(rotZ);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;

            double x = radius * Math.cos(angle);
            double y = 0;
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

            packetArmorStand.setLocation(
                    newLoc.getX(),
                    newLoc.getY(),
                    newLoc.getZ(),
                    packetArmorStand.yaw,
                    packetArmorStand.pitch
            );

            PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
        }
    }
}

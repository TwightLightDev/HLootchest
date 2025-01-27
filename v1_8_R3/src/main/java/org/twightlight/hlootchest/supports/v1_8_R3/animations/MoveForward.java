package org.twightlight.hlootchest.supports.v1_8_R3.animations;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;


public class MoveForward {
    public MoveForward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.yaw;

        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        packetArmorStand.setLocation(
                packetArmorStand.locX + dx,
                packetArmorStand.locY,
                packetArmorStand.locZ + dz,
                yaw,
                packetArmorStand.pitch
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
    }
}

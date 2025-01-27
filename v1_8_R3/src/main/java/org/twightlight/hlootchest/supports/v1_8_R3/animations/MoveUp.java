package org.twightlight.hlootchest.supports.v1_8_R3.animations;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class MoveUp {
    public MoveUp(Player player, EntityArmorStand packetArmorStand, float val) {

        packetArmorStand.setLocation(
                packetArmorStand.locX,
                packetArmorStand.locY + val,
                packetArmorStand.locZ,
                packetArmorStand.yaw,
                packetArmorStand.pitch
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
    }
}

package org.twightlight.hlootchest.supports.v1_8_R3.animations;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Spinning {
    public Spinning(Player player, EntityArmorStand packetArmorStand, float val) {

        while (val > 180) {
            val -= 360;
        }

        while (val < -180) {
            val += 360;
        }

        packetArmorStand.setLocation(
                packetArmorStand.locX,
                packetArmorStand.locY,
                packetArmorStand.locZ,
                val,
                packetArmorStand.pitch
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
    }
}

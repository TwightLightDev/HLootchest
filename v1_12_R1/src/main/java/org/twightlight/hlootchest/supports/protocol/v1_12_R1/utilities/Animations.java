package org.twightlight.hlootchest.supports.protocol.v1_12_R1.utilities;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class Animations {
    public static void DrawCircle(Player player, EntityArmorStand packetArmorStand, Location center, double radius, int points, double rotX, double rotY, double rotZ) {

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

    public static void MoveBackward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.yaw;
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        packetArmorStand.setLocation(
                packetArmorStand.locX - dx,
                packetArmorStand.locY,
                packetArmorStand.locZ - dz,
                yaw,
                packetArmorStand.pitch
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
    }

    public static void MoveForward(Player player, EntityArmorStand packetArmorStand, float val) {
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

    public static void MoveUp(Player player, EntityArmorStand packetArmorStand, float val) {

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

    public static void Spinning(Player player, EntityArmorStand packetArmorStand, float val) {

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

        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                packetArmorStand.getId(),
                (byte) ((val * 256.0F) / 360.0F),
                (byte) ((packetArmorStand.pitch * 256.0F) / 360.0F),
                true // OnGround flag
        );
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(lookPacket);

    }

    public static void spawnFireWork(Player player, Location location, FireworkEffect effect) {
        ItemStack sF = new ItemStack(Material.FIREWORK);
        FireworkMeta fireworkMeta = (FireworkMeta) sF.getItemMeta();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(2);
        sF.setItemMeta(fireworkMeta);
        EntityFireworks firework = new EntityFireworks(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(sF));

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntity(firework, 76));
        firework.expectedLifespan = 0;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(firework.getId(), firework.getDataWatcher(), true));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus(firework, (byte) 17));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(firework.getId()));
        player.playSound(location, Sound.ENTITY_FIREWORK_BLAST, 10.0f, 10.0f);
    }
}

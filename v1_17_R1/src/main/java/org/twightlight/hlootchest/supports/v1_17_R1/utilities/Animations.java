package org.twightlight.hlootchest.supports.v1_17_R1.utilities;

import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.projectile.EntityFireworks;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.twightlight.hlootchest.supports.v1_17_R1.Main;

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
            packetArmorStand.setPositionRaw(newLoc.getX(), newLoc.getY(), newLoc.getZ());

            PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
            ((CraftPlayer) player).getHandle().b.sendPacket(teleportPacket);
        }
    }

    public static void MoveBackward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        packetArmorStand.setPositionRaw(
                packetArmorStand.locX() - dx,
                packetArmorStand.locY(),
                packetArmorStand.locZ() - dz
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().b.sendPacket(teleportPacket);
    }

    public static void MoveForward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;

        packetArmorStand.setPositionRaw(
                packetArmorStand.locX() + dx,
                packetArmorStand.locY(),
                packetArmorStand.locZ() + dz
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().b.sendPacket(teleportPacket);
    }

    public static void MoveUp(Player player, EntityArmorStand packetArmorStand, float val) {

        packetArmorStand.setPositionRaw(
                packetArmorStand.locX(),
                packetArmorStand.locY() + val,
                packetArmorStand.locZ()
        );

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().b.sendPacket(teleportPacket);
    }

    public static void Spinning(Player player, EntityArmorStand packetArmorStand, float val) {
        while (val > 180) {
            val -= 360;
        }
        while (val < -180) {
            val += 360;
        }

        packetArmorStand.setYRot(val);

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(packetArmorStand);
        ((CraftPlayer) player).getHandle().b.sendPacket(teleportPacket);

        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                packetArmorStand.getId(),
                (byte) ((val * 256.0F) / 360.0F),
                (byte) ((packetArmorStand.getXRot() * 256.0F) / 360.0F),
                true
        );
        ((CraftPlayer) player).getHandle().b.sendPacket(lookPacket);
    }

    public static void spawnFireWork(Player player, Location location, FireworkEffect effect) {
        org.bukkit.inventory.ItemStack sF = new org.bukkit.inventory.ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta) sF.getItemMeta();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(2);
        sF.setItemMeta(fireworkMeta);

        EntityFireworks firework = new EntityFireworks(EntityTypes.D, ((CraftWorld) location.getWorld()).getHandle());
        firework.setPositionRaw(location.getX(), location.getY(), location.getZ());

        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutSpawnEntity(firework));
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(firework.getId(), firework.getDataWatcher(), true));
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutEntityStatus(firework, (byte) 17));
        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
            ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutEntityDestroy(firework.getId()));
        }, 1L);
        player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 10.0f, 10.0f);
    }
}

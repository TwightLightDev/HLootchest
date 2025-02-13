// Decompiled with: CFR 0.152
// Class Version: 8
package org.twightlight.hlootchest.supports.v1_19_R3.utilities;

import com.cryptomorin.xseries.XSound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

public class Animations {
    public static void DrawCircle(Player player, EntityArmorStand packetArmorStand, Location center, double radius, int points, double rotX, double rotY, double rotZ) {
        double radiansRotX = Math.toRadians(rotX);
        double radiansRotY = Math.toRadians(rotY);
        double radiansRotZ = Math.toRadians(rotZ);
        for (int i = 0; i < points; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)points;
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
            packetArmorStand.p(newLoc.getX(), newLoc.getY(), newLoc.getZ());
            PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
            ((CraftPlayer)player).getHandle().b.a((Packet)teleportPacket);
        }
    }

    public static void MoveBackward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * (double)val;
        double dz = Math.cos(Math.toRadians(yaw)) * (double)val;
        packetArmorStand.p(packetArmorStand.dl() - dx, packetArmorStand.dn(), packetArmorStand.dr() - dz);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        ((CraftPlayer)player).getHandle().b.a((Packet)teleportPacket);
    }

    public static void MoveForward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * (double)val;
        double dz = Math.cos(Math.toRadians(yaw)) * (double)val;
        packetArmorStand.p(packetArmorStand.dl() + dx, packetArmorStand.dn(), packetArmorStand.dr() + dz);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        ((CraftPlayer)player).getHandle().b.a((Packet)teleportPacket);
    }

    public static void MoveUp(Player player, EntityArmorStand packetArmorStand, float val) {
        packetArmorStand.p(packetArmorStand.dl(), packetArmorStand.dn() + (double)val, packetArmorStand.dr());
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        ((CraftPlayer)player).getHandle().b.a((Packet)teleportPacket);
    }

    public static void Spinning(Player player, EntityArmorStand packetArmorStand, float val) {
        while (val > 180.0f) {
            val -= 360.0f;
        }
        while (val < -180.0f) {
            val += 360.0f;
        }
        packetArmorStand.f(val);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        ((CraftPlayer)player).getHandle().b.a((Packet)teleportPacket);
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(packetArmorStand.af(), (byte)(val * 256.0f / 360.0f), (byte)(packetArmorStand.dy() * 256.0f / 360.0f), true);
        ((CraftPlayer)player).getHandle().b.a((Packet)lookPacket);
    }

    public static void spawnFireWork(Player player, Location location, FireworkEffect effect) {
        ItemStack sF = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta)sF.getItemMeta();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(2);
        sF.setItemMeta(fireworkMeta);
        EntityFireworks firework = new EntityFireworks(EntityTypes.M, (World)((CraftWorld)location.getWorld()).getHandle());
        firework.p(location.getX(), location.getY(), location.getZ());
        ((CraftPlayer)player).getHandle().b.a(new PacketPlayOutSpawnEntity((Entity)firework));
        ((CraftPlayer)player).getHandle().b.a(new PacketPlayOutEntityMetadata(firework.af(), firework.aj().c()));
        ((CraftPlayer)player).getHandle().b.a(new PacketPlayOutEntityStatus(firework, Byte.valueOf((byte)17)));
        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> ((CraftPlayer)player).getHandle().b.a((Packet)new PacketPlayOutEntityDestroy(new int[]{firework.af()})), 1L);
        player.playSound(location, XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(), 10.0f, 10.0f);
    }
}

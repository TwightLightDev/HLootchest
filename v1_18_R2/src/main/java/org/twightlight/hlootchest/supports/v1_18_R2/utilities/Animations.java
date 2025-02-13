package org.twightlight.hlootchest.supports.v1_18_R2.utilities;

import com.cryptomorin.xseries.XSound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.twightlight.hlootchest.supports.v1_18_R2.Main;

public class Animations {
    public static void DrawCircle(Player player, EntityArmorStand packetArmorStand, Location center, double radius, int points, double rotX, double rotY, double rotZ) {
        double radiansRotX = Math.toRadians(rotX);
        double radiansRotY = Math.toRadians(rotY);
        double radiansRotZ = Math.toRadians(rotZ);
        for (int i = 0; i < points; i++) {
            double angle = 6.283185307179586D * i / points;
            double x = radius * Math.cos(angle);
            double y = 0.0D;
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
            packetArmorStand.o(newLoc.getX(), newLoc.getY(), newLoc.getZ());
            PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
            (((CraftPlayer)player).getHandle()).b.a((Packet)teleportPacket);
        }
    }

    public static void MoveBackward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;
        packetArmorStand.o(packetArmorStand
                .dc() - dx, packetArmorStand
                .de(), packetArmorStand
                .di() - dz);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        (((CraftPlayer)player).getHandle()).b.a((Packet)teleportPacket);
    }

    public static void MoveForward(Player player, EntityArmorStand packetArmorStand, float val) {
        float yaw = packetArmorStand.getBukkitYaw();
        double dx = -Math.sin(Math.toRadians(yaw)) * val;
        double dz = Math.cos(Math.toRadians(yaw)) * val;
        packetArmorStand.o(packetArmorStand
                .dc() + dx, packetArmorStand
                .de(), packetArmorStand
                .di() + dz);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        (((CraftPlayer)player).getHandle()).b.a((Packet)teleportPacket);
    }

    public static void MoveUp(Player player, EntityArmorStand packetArmorStand, float val) {
        packetArmorStand.o(packetArmorStand
                .dc(), packetArmorStand
                .de() + val, packetArmorStand
                .di());
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        (((CraftPlayer)player).getHandle()).b.a((Packet)teleportPacket);
    }

    public static void Spinning(Player player, EntityArmorStand packetArmorStand, float val) {
        while (val > 180.0F)
            val -= 360.0F;
        while (val < -180.0F)
            val += 360.0F;
        packetArmorStand.o(val);
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport((Entity)packetArmorStand);
        (((CraftPlayer)player).getHandle()).b.a((Packet)teleportPacket);


        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(packetArmorStand.ae(), (byte)(int)(val * 256.0F / 360.0F), (byte)(int)(Main.getPitch(packetArmorStand) * 256.0F / 360.0F), true);
        (((CraftPlayer)player).getHandle()).b.a((Packet)lookPacket);
    }



    public static void spawnFireWork(Player player, Location location, FireworkEffect effect) {
        ItemStack sF = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta)sF.getItemMeta();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(2);
        sF.setItemMeta((ItemMeta)fireworkMeta);
        EntityFireworks firework = new EntityFireworks(EntityTypes.D, (World)((CraftWorld)location.getWorld()).getHandle());
        firework.o(location.getX(), location.getY(), location.getZ());
        (((CraftPlayer)player).getHandle()).b.a((Packet)new PacketPlayOutSpawnEntity((Entity)firework));
        (((CraftPlayer)player).getHandle()).b.a((Packet)new PacketPlayOutEntityMetadata(firework.ae(), firework.ai(), true));
        (((CraftPlayer)player).getHandle()).b.a((Packet)new PacketPlayOutEntityStatus((Entity)firework, (byte)17));
        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
            ((CraftPlayer) player).getHandle().b.a(new PacketPlayOutEntityDestroy(new int[]{firework.ae()}));
        }, 1L);
        player.playSound(location, XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(), 10.0F, 10.0F);
    }
}

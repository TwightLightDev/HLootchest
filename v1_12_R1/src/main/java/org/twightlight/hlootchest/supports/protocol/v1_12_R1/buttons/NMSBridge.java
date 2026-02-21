package org.twightlight.hlootchest.supports.protocol.v1_12_R1.buttons;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.Main;
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.utilities.Animations;

public class NMSBridge implements org.twightlight.hlootchest.api.buttons.NMSBridge {

    @Override
    public ArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable, float yaw, float pitch) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());
        stand.setCustomNameVisible(isNameEnable);
        stand.setCustomName(name);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setSmall(isSmall);
        stand.yaw = yaw;
        stand.pitch = pitch;
        return (ArmorStand) stand.getBukkitEntity();
    }

    @Override
    public void sendSpawnPacket(Player player, ArmorStand armorStand) {
        EntityArmorStand nms = getNMS(armorStand);
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(nms);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendDespawnPacket(Player player, ArmorStand armorStand) {
        EntityArmorStand nms = getNMS(armorStand);
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nms.getId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void equipIcon(Player player, ArmorStand armorStand, ItemStack item, ItemSlot slot) {
        Main.nmsUtil.equipIcon(player, getNMS(armorStand), item, slot);
    }

    @Override
    public void sendMetadataPacket(Player player, ArmorStand armorStand) {
        EntityArmorStand nms = getNMS(armorStand);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(nms.getId(), nms.getDataWatcher(), true);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void setCustomName(ArmorStand armorStand, String name) {
        getNMS(armorStand).setCustomName(name);
    }

    @Override
    public void setNameVisible(ArmorStand armorStand, boolean visible) {
        getNMS(armorStand).setCustomNameVisible(visible);
    }

    @Override
    public void rotate(ArmorStand armorStand, TYamlWrapper config, String path) {
        Main.nmsUtil.rotate(getNMS(armorStand), config, path);
    }

    @Override
    public void moveForward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveForward(player, getNMS(armorStand), val);
    }

    @Override
    public void moveBackward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveBackward(player, getNMS(armorStand), val);
    }

    @Override
    public void spin(Player player, ArmorStand armorStand, float yaw) {
        Animations.Spinning(player, getNMS(armorStand), yaw);
    }

    @Override
    public double getX(ArmorStand armorStand) { return getNMS(armorStand).locX; }
    @Override
    public double getY(ArmorStand armorStand) { return getNMS(armorStand).locY; }
    @Override
    public double getZ(ArmorStand armorStand) { return getNMS(armorStand).locZ; }
    @Override
    public float getYaw(ArmorStand armorStand) { return getNMS(armorStand).yaw; }
    @Override
    public Location getBukkitLocation(ArmorStand armorStand) { return armorStand.getLocation(); }

    private EntityArmorStand getNMS(ArmorStand armorStand) {
        return ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand) armorStand).getHandle();
    }
}



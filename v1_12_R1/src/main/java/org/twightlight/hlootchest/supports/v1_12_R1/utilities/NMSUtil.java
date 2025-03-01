package org.twightlight.hlootchest.supports.v1_12_R1.utilities;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.NMSService;
import org.twightlight.hlootchest.supports.v1_12_R1.Main;

public class NMSUtil implements NMSService {
    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.p(p, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return (ArmorStand) armorStand.getBukkitEntity();
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {

        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nmsEntity.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {
            EntityArmorStand nmsEntity = ((CraftArmorStand) entityLiving).getHandle();
            net.minecraft.server.v1_12_R1.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            EnumItemSlot slotint = EnumItemSlot.MAINHAND;
            switch (slot) {
                case HEAD:
                    slotint = EnumItemSlot.HEAD;
                    break;
                case CHESTPLATE:
                    slotint = EnumItemSlot.CHEST;
                    break;
                case LEGGINGS:
                    slotint = EnumItemSlot.LEGS;
                    break;
                case BOOTS:
                    slotint = EnumItemSlot.FEET;
                    break;
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    slotint = EnumItemSlot.OFFHAND;
                    break;
            }
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                    nmsEntity.getId(),
                    slotint,
                    icon
            );
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }
    public void drawCircle(Player player, ArmorStand armorStand, Location center, double radius, double rotX, double rotY, double rotZ, int points) {
        Animations.DrawCircle(player, ((CraftArmorStand) armorStand).getHandle(), center, radius, points, rotX, rotY, rotZ);
    }

    public void moveBackward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveBackward(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }

    public void moveForward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveForward(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }

    public void moveUp(Player player, ArmorStand armorStand, double val) {
        Animations.MoveUp(player, ((CraftArmorStand) armorStand).getHandle(), (float) val);
    }

    public void spin(Player player, ArmorStand armorStand, float val) {
        Animations.Spinning(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }
}

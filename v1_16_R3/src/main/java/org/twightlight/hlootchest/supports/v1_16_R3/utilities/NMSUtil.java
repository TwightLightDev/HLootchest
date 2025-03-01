package org.twightlight.hlootchest.supports.v1_16_R3.utilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.supports.v1_16_R3.Main;

import java.util.Collections;

public class NMSUtil implements NMSService {
    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.IChatBaseComponentfromString(Main.p(p, ChatColor.translateAlternateColorCodes('&', name))));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return (ArmorStand) armorStand.getBukkitEntity();
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {

        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            nmsEntity.getDataWatcher().set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20);
            PacketPlayOutEntityMetadata packet1 = new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet1);
        }
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nmsEntity.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void equipIcon(Player p, ArmorStand entityLiving, TIcon icon) {
        equipIcon(p, entityLiving, icon.getItemStack(), icon.getItemSlot());
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        equipIcon(p, ((CraftArmorStand) entityLiving).getHandle(), bukkiticon, slot);
    }

    public void equipIcon(Player p, EntityArmorStand nmsEntity, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {
            net.minecraft.server.v1_16_R3.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
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
                    Collections.singletonList(new Pair<>(slotint, icon))
            );
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T summonVehicle(Location loc, Class<T> entityClass) {
        EntityType entityType = getEntityType(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException("Unsupported entity class: " + entityClass.getName());
        }
        Entity vehicle = loc.getWorld().spawnEntity(loc.clone().add(0, -0.3, 0), entityType);

        vehicle.setCustomName("LootchestVehicle");
        vehicle.setCustomNameVisible(false);
        vehicle.setSilent(true);
        vehicle.setInvulnerable(true);
        vehicle.setGravity(false);
        if (vehicle instanceof LivingEntity) {
            ((LivingEntity) vehicle).setAI(false);
            ((LivingEntity) vehicle).setInvisible(true);
            ((LivingEntity) vehicle).setCollidable(false);
        }
        return (T) vehicle;
    }

    private EntityType getEntityType(Class<? extends Entity> entityClass) {
        for (EntityType type : EntityType.values()) {
            if (type.getEntityClass() != null && type.getEntityClass().equals(entityClass)) {
                return type;
            }
        }
        return null;
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

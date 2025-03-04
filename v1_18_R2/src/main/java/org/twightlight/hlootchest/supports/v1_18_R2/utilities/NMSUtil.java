package org.twightlight.hlootchest.supports.v1_18_R2.utilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.supports.v1_18_R2.Main;

import java.util.Collections;

public class NMSUtil implements NMSService {
    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        return createArmorStand(p, location, name, false, isNameEnable);
    }

    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isSmall, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());
        armorStand.n(isNameEnable);
        armorStand.a(IChatBaseComponent.a(Main.p(p, ChatColor.translateAlternateColorCodes('&', name))));
        armorStand.j(true);
        armorStand.e(true);
        armorStand.a(isSmall);

        armorStand.o(location.getYaw());
        armorStand.p(location.getPitch());
        return (ArmorStand) armorStand.getBukkitEntity();
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {

        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();

        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(nmsEntity);
        (((CraftPlayer)player).getHandle()).b.a(packet);

        nmsEntity.ai().b(new DataWatcherObject(0, DataWatcherRegistry.a), Byte.valueOf((byte)32));
        PacketPlayOutEntityMetadata packet1 = new PacketPlayOutEntityMetadata(nmsEntity.ae(), nmsEntity.ai(), true);
        (((CraftPlayer)player).getHandle()).b.a(packet1);
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();

        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { nmsEntity.ae() });
        (((CraftPlayer)player).getHandle()).b.a(packet);
    }

    public void equipIcon(Player p, ArmorStand entityLiving, TIcon icon) {
        equipIcon(p, entityLiving, icon.getItemStack(), icon.getItemSlot());
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        equipIcon(p, ((CraftArmorStand) entityLiving).getHandle(), bukkiticon, slot);
    }

    public void equipIcon(Player p, EntityArmorStand nmsEntity, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {

            net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            EnumItemSlot slotint = EnumItemSlot.a;
            switch (slot) {
                case HEAD:
                    slotint = EnumItemSlot.f;
                    break;
                case CHESTPLATE:
                    slotint = EnumItemSlot.e;
                    break;
                case LEGGINGS:
                    slotint = EnumItemSlot.d;
                    break;
                case BOOTS:
                    slotint = EnumItemSlot.c;
                    break;
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    slotint = EnumItemSlot.b;
                    break;
            }
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                    nmsEntity.ae(),
                    Collections.singletonList(new Pair(slotint, icon)));
            (((CraftPlayer)p).getHandle()).b.a(packet);
        }
    }

    public void teleport(Player player, Entity entityLiving, Location location) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        nmsEntity.o(
                location.getX(),
                location.getY(),
                location.getZ()
        );
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(nmsEntity);
        ((CraftPlayer) player).getHandle().b.a(packet);
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                nmsEntity.ae(),
                (byte) ((location.getYaw() * 256.0F) / 360.0F),
                (byte) ((location.getPitch() * 256.0F) / 360.0F),
                true
        );
        ((CraftPlayer) player).getHandle().b.a(lookPacket);
    }

    public void lockAngle(Player p, Location loc, long duration) {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > duration * 50)
                    cancel();
                if (p.getLocation().getYaw() != loc.getYaw() || p.getLocation().getPitch() != loc.getPitch()) {
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 2L);
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

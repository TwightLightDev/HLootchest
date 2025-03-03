package org.twightlight.hlootchest.supports.v1_8_R3.utilities;

import com.cryptomorin.xseries.XPotion;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;

import java.util.HashSet;

public class NMSUtil implements NMSService {

    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.p(p, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return (ArmorStand) armorStand.getBukkitEntity();
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nmsEntity.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void teleport(Player player, Entity entityLiving, Location location) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        nmsEntity.setLocation(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(nmsEntity);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void lockAngle(Player p, Location loc, long duration) {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > duration * 50)
                    return;
                if (p.getLocation().getYaw() != loc.getYaw() || p.getLocation().getPitch() != loc.getPitch()) {
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 2L);
    }

    public void equipIcon(Player p, ArmorStand entityLiving, TIcon icon) {
        equipIcon(p, entityLiving, icon.getItemStack(), icon.getItemSlot());
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        equipIcon(p, ((CraftArmorStand) entityLiving).getHandle(), bukkiticon, slot);
    }

    public void equipIcon(Player p, EntityArmorStand nmsEntity, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {
            net.minecraft.server.v1_8_R3.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            int slotint = 0;
            switch (slot) {
                case HEAD:
                    slotint = 4;
                    break;
                case CHESTPLATE:
                    slotint = 3;
                    break;
                case LEGGINGS:
                    slotint = 2;
                    break;
                case BOOTS:
                    slotint = 1;
                    break;
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    slotint = 5;
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

    @SuppressWarnings("unchecked")
    public <T extends Entity> T summonVehicle(Location loc, Class<T> entityClass) {
        EntityType entityType = getEntityType(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException("Unsupported entity class: " + entityClass.getName());
        }

        Entity vehicle = loc.getWorld().spawnEntity(loc.clone().add(0, -0.3, 0), entityType);

        if (vehicle instanceof LivingEntity) {
            ((LivingEntity) vehicle).addPotionEffect(new PotionEffect(XPotion.INVISIBILITY.getPotionEffectType(), Integer.MAX_VALUE, 1, false, false));
        }

        vehicle.setCustomName("LootchestVehicle");
        vehicle.setCustomNameVisible(false);

        if (vehicle instanceof CraftEntity) {
            net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) vehicle).getHandle();
            NBTTagCompound tag = nmsEntity.getNBTTag();

            if (tag == null) {
                tag = new NBTTagCompound();
            }

            nmsEntity.c(tag);
            tag.setInt("NoAI", 1);
            tag.setInt("NoGravity", 1);
            nmsEntity.f(tag);
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

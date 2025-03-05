package org.twightlight.hlootchest.supports.v1_19_R3.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

public class NMSUtil implements NMSService {
    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        return createArmorStand(p, location, name, false, isNameEnable);
    }

    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isSmall, boolean isNameEnable) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.p(p, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setVisibleByDefault(false);
        armorStand.setSmall(isSmall);
        armorStand.setMetadata("removeOnRestart", new FixedMetadataValue(Main.handler.plugin, true));
        return armorStand;
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {
        player.showEntity(Main.handler.plugin, entityLiving);
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {
        player.hideEntity(Main.handler.plugin, entityLiving);

    }

    public void equipIcon(Player p, ArmorStand entityLiving, TIcon icon) {
        equipIcon(p, entityLiving, icon.getItemStack(), icon.getItemSlot());
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {
            EquipmentSlot slotint = EquipmentSlot.HAND;
            switch (slot) {
                case HEAD:
                    slotint = EquipmentSlot.HEAD;
                    break;
                case CHESTPLATE:
                    slotint = EquipmentSlot.CHEST;
                    break;
                case LEGGINGS:
                    slotint = EquipmentSlot.LEGS;
                    break;
                case BOOTS:
                    slotint = EquipmentSlot.FEET;
                    break;
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    slotint = EquipmentSlot.OFF_HAND;
                    break;
            }

            entityLiving.getEquipment().setItem(slotint, bukkiticon);
        }
    }

    public void teleport(Player player, Entity entityLiving, Location location) {
        entityLiving.teleport(location);
    }

    public void lockAngle(Player p, Location loc, long duration) {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > duration * 50)
                    cancel();
                if (p.getLocation().getYaw() != loc.getYaw() || p.getLocation().getPitch() != loc.getPitch()) {
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Main.api.getDebugService().sendDebugMsg(p, "Attempt to teleport " + p + "to " + loc);
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
        Animations.drawCircle(player, armorStand, center, radius, rotX, rotY, rotZ, points);
    }

    public void moveBackward(Player player, ArmorStand armorStand, float val) {
        Animations.moveBackward(player, armorStand, val);
    }

    public void moveForward(Player player, ArmorStand armorStand, float val) {
        Animations.moveForward(player, armorStand, val);
    }

    public void moveUp(Player player, ArmorStand armorStand, double val) {
        Animations.moveUp(player, armorStand, val);
    }

    public void spin(Player player, ArmorStand armorStand, float val) {
        Animations.spin(player, armorStand, val);
    }
}

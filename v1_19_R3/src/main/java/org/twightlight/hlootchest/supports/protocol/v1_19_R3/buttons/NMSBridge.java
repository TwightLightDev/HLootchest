package org.twightlight.hlootchest.supports.protocol.v1_19_R3.buttons;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.utilities.Animations;

import java.util.Set;

public class NMSBridge implements org.twightlight.hlootchest.api.buttons.NMSBridge {

    @Override
    public ArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable, float yaw, float pitch) {
        Location loc = location.clone();
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(name);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setSmall(isSmall);
        armorStand.setVisibleByDefault(false);
        return armorStand;
    }

    @Override
    public void sendSpawnPacket(Player player, ArmorStand armorStand) {
        player.showEntity(Main.handler.plugin, armorStand);
    }

    @Override
    public void sendDespawnPacket(Player player, ArmorStand armorStand) {
        player.hideEntity(Main.handler.plugin, armorStand);
    }

    @Override
    public void equipIcon(Player player, ArmorStand armorStand, ItemStack item, ItemSlot slot) {
        if (item == null) return;
        EquipmentSlot equipSlot;
        switch (slot) {
            case HEAD:
                equipSlot = EquipmentSlot.HEAD;
                break;
            case CHESTPLATE:
                equipSlot = EquipmentSlot.CHEST;
                break;
            case LEGGINGS:
                equipSlot = EquipmentSlot.LEGS;
                break;
            case BOOTS:
                equipSlot = EquipmentSlot.FEET;
                break;
            case OFF_HAND:
                equipSlot = EquipmentSlot.OFF_HAND;
                break;
            case MAIN_HAND:
            default:
                equipSlot = EquipmentSlot.HAND;
                break;
        }
        armorStand.getEquipment().setItem(equipSlot, item);
    }

    @Override
    public void sendMetadataPacket(Player player, ArmorStand armorStand) {
    }

    @Override
    public void setCustomName(ArmorStand armorStand, String name) {
        armorStand.setCustomName(name);
    }

    @Override
    public void setNameVisible(ArmorStand armorStand, boolean visible) {
        armorStand.setCustomNameVisible(visible);
    }

    @Override
    public void rotate(ArmorStand armorStand, TYamlWrapper config, String path) {
        if (config.getYml().getConfigurationSection(path + ".rotations") != null) {
            Set<String> rotations = config.getYml().getConfigurationSection(path + ".rotations").getKeys(false);
            for (String s : rotations) {
                String rotationString = config.getString(path + ".rotations." + s + ".value");
                EulerAngle rotation = Main.stringToVector3f(rotationString);
                String position = config.getString(path + ".rotations." + s + ".position");
                switch (position) {
                    case "HEAD":
                        armorStand.setHeadPose(rotation);
                        break;
                    case "BODY":
                        armorStand.setBodyPose(rotation);
                        break;
                    case "RIGHT_ARM":
                        armorStand.setRightArmPose(rotation);
                        break;
                    case "LEFT_ARM":
                        armorStand.setLeftArmPose(rotation);
                        break;
                    case "RIGHT_LEG":
                        armorStand.setRightLegPose(rotation);
                        break;
                    case "LEFT_LEG":
                        armorStand.setLeftLegPose(rotation);
                        break;
                }
            }
        }
    }

    @Override
    public void moveForward(Player player, ArmorStand armorStand, float val) {
        Animations.moveForward(player, armorStand, val);
    }

    @Override
    public void moveBackward(Player player, ArmorStand armorStand, float val) {
        Animations.moveBackward(player, armorStand, val);
    }

    @Override
    public void spin(Player player, ArmorStand armorStand, float yaw) {
        Animations.spin(player, armorStand, yaw);
    }

    @Override
    public double getX(ArmorStand armorStand) {
        return armorStand.getLocation().getX();
    }

    @Override
    public double getY(ArmorStand armorStand) {
        return armorStand.getLocation().getY();
    }

    @Override
    public double getZ(ArmorStand armorStand) {
        return armorStand.getLocation().getZ();
    }

    @Override
    public float getYaw(ArmorStand armorStand) {
        return armorStand.getLocation().getYaw();
    }

    @Override
    public Location getBukkitLocation(ArmorStand armorStand) {
        return armorStand.getLocation();
    }
}


package org.twightlight.hlootchest.api.buttons;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;

public interface NMSBridge {
    ArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable, float yaw, float pitch);
    void sendSpawnPacket(Player player, ArmorStand armorStand);
    void sendDespawnPacket(Player player, ArmorStand armorStand);
    void equipIcon(Player player, ArmorStand armorStand, ItemStack item, ItemSlot slot);
    void sendMetadataPacket(Player player, ArmorStand armorStand);
    void setCustomName(ArmorStand armorStand, String name);
    void setNameVisible(ArmorStand armorStand, boolean visible);
    void rotate(ArmorStand armorStand, TYamlWrapper config, String path);
    void moveForward(Player player, ArmorStand armorStand, float val);
    void moveBackward(Player player, ArmorStand armorStand, float val);
    void spin(Player player, ArmorStand armorStand, float yaw);
    double getX(ArmorStand armorStand);
    double getY(ArmorStand armorStand);
    double getZ(ArmorStand armorStand);
    float getYaw(ArmorStand armorStand);
    Location getBukkitLocation(ArmorStand armorStand);
}


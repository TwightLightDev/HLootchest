package org.twightlight.hlootchest.api.interfaces.internal;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;

public interface NMSService {
    ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable);
    void sendSpawnPacket(Player player, Entity entityLiving);
    void sendDespawnPacket(Player player, Entity entityLiving);
    void equipIcon(Player p, ArmorStand entityLiving, TIcon icon);
    void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot);
    <T extends Entity> T summonVehicle(Location loc, Class<T> entityClass);
    void drawCircle(Player player, ArmorStand armorStand, Location center, double radius, double rotX, double rotY, double rotZ, int points);
    void moveBackward(Player player, ArmorStand armorStand, float val);
    void moveForward(Player player, ArmorStand armorStand, float val);
    void moveUp(Player player, ArmorStand armorStand, double val);
    void spin(Player player, ArmorStand armorStand, float val);
}

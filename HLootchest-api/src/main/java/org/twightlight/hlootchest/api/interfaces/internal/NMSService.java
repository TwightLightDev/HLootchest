package org.twightlight.hlootchest.api.interfaces.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;

public interface NMSService {
    /**
     * Creates an {@link ArmorStand} at the specified location with given properties.
     *
     * @param p The {@link Player} creating the armor stand.
     * @param location The {@link Location} where the armor stand should be created.
     * @param name The display name of the armor stand.
     * @param isSmall {@code true} if the armor stand should be small, {@code false} otherwise.
     * @param isNameEnable {@code true} if the name should be visible, {@code false} otherwise.
     * @return The created {@link ArmorStand} instance.
     */
    ArmorStand createArmorStand(Player p, Location location, String name, boolean isSmall, boolean isNameEnable);

    /**
     * Creates an {@link ArmorStand} at the specified location with a name and visibility setting.
     *
     * @param p The {@link Player} creating the armor stand.
     * @param location The {@link Location} where the armor stand should be created.
     * @param name The display name of the armor stand.
     * @param isNameEnable {@code true} if the name should be visible, {@code false} otherwise.
     * @return The created {@link ArmorStand} instance.
     */
    ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable);

    /**
     * Sends a spawn packet to a player for a specific entity.
     *
     * @param player The {@link Player} who should receive the spawn packet.
     * @param entityLiving The {@link Entity} to spawn.
     */
    void sendSpawnPacket(Player player, Entity entityLiving);

    /**
     * Sends a despawn packet to a player for a specific entity.
     *
     * @param player The {@link Player} who should receive the despawn packet.
     * @param entityLiving The {@link Entity} to despawn.
     */
    void sendDespawnPacket(Player player, Entity entityLiving);

    /**
     * Equips an {@link ArmorStand} with a custom icon.
     *
     * @param p The {@link Player} modifying the armor stand.
     * @param entityLiving The {@link ArmorStand} to equip.
     * @param icon The {@link TIcon} to set on the armor stand.
     */
    void equipIcon(Player p, ArmorStand entityLiving, TIcon icon);

    /**
     * Equips an {@link ArmorStand} with a specified item in a specific slot.
     *
     * @param p The {@link Player} modifying the armor stand.
     * @param entityLiving The {@link ArmorStand} to equip.
     * @param bukkiticon The {@link ItemStack} to place on the armor stand.
     * @param slot The {@link ItemSlot} where the item should be placed.
     */
    void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot);

    /**
     * Teleports an entity to a specified location.
     *
     * @param player The {@link Player} executing the teleportation.
     * @param entityLiving The {@link Entity} to teleport.
     * @param location The destination {@link Location}.
     */
    void teleport(Player player, Entity entityLiving, Location location);

    /**
     * Locks the player's view to a specific angle for a given duration.
     *
     * @param p The {@link Player} whose view will be locked.
     * @param loc The {@link Location} the player should look at.
     * @param duration The duration in milliseconds to lock the view.
     */
    void lockAngle(Player p, Location loc, long duration);

    /**
     * Summons a vehicle of the specified type at a given location.
     *
     * @param loc The {@link Location} where the vehicle should be summoned.
     * @param entityClass The class type of the entity to summon.
     * @param <T> The entity type that extends {@link Entity}.
     * @return The summoned vehicle instance.
     */
    <T extends Entity> T summonVehicle(Location loc, Class<T> entityClass);

    /**
     * Draws a circular path around an {@link ArmorStand} with a specified rotation.
     *
     * @param player The {@link Player} who initiates the drawing.
     * @param armorStand The {@link ArmorStand} at the center of the circle.
     * @param center The {@link Location} of the circle's center.
     * @param radius The radius of the circle.
     * @param rotX Rotation on the X-axis.
     * @param rotY Rotation on the Y-axis.
     * @param rotZ Rotation on the Z-axis.
     * @param points The number of points defining the circle.
     */
    void drawCircle(Player player, ArmorStand armorStand, Location center, double radius, double rotX, double rotY, double rotZ, int points);

    /**
     * Moves an {@link ArmorStand} backward by a specified amount.
     *
     * @param player The {@link Player} controlling the movement.
     * @param armorStand The {@link ArmorStand} to move.
     * @param val The distance to move backward.
     */
    void moveBackward(Player player, ArmorStand armorStand, float val);

    /**
     * Moves an {@link ArmorStand} forward by a specified amount.
     *
     * @param player The {@link Player} controlling the movement.
     * @param armorStand The {@link ArmorStand} to move.
     * @param val The distance to move forward.
     */
    void moveForward(Player player, ArmorStand armorStand, float val);

    /**
     * Moves an {@link ArmorStand} upwards by a specified amount.
     *
     * @param player The {@link Player} controlling the movement.
     * @param armorStand The {@link ArmorStand} to move.
     * @param val The distance to move upwards.
     */
    void moveUp(Player player, ArmorStand armorStand, double val);

    /**
     * Rotates an {@link ArmorStand} around its vertical axis.
     *
     * @param player The {@link Player} controlling the rotation.
     * @param armorStand The {@link ArmorStand} to spin.
     * @param val The angle to rotate by, in degrees.
     */
    void spin(Player player, ArmorStand armorStand, float val);

    /**
     * Sets a block at a specific location with a given material and facing direction.
     *
     * @param p The {@link Player} performing the action.
     * @param loc The {@link Location} of the block to change.
     * @param to The {@link Material} to set at the location.
     * @param facing The {@link BlockFace} direction the block should face.
     */
    void setNmsBlock(Player p, Location loc, Material to, BlockFace facing);
}

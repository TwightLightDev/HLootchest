package org.twightlight.hlootchest.api.interfaces.lootchest;

import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;

import java.util.List;
import java.util.Map;

public interface TBox {
    /**
     * Opens the box.
     *
     * @return {@code true} if the box was successfully opened, otherwise {@code false}.
     */
    boolean open();

    /**
     * Removes the box from the game.
     */
    void remove();

    /**
     * Sets the icon of the box to the specified {@link ItemStack}.
     *
     * @param icon The {@link ItemStack} to set as the box's icon.
     */
    void equipIcon(ItemStack icon);

    /**
     * Checks whether the box is clickable.
     *
     * @return {@code true} if the box is clickable, otherwise {@code false}.
     */
    boolean isClickable();

    /**
     * Sets whether the box is clickable.
     *
     * @param state {@code true} to make the box clickable, {@code false} otherwise.
     */
    void setClickable(boolean state);

    /**
     * Gets the owner of the box.
     *
     * @return The {@link Player} who owns the box.
     */
    Player getOwner();

    /**
     * Gets the location of the box.
     *
     * @return The {@link Location} of the box.
     */
    Location getLoc();

    /**
     * Gets the icon of the box.
     *
     * @return The {@link ItemStack} representing the box's icon.
     */
    ItemStack getIcon();

    /**
     * Gets the list of current vehicles associated with the box.
     *
     * @return A {@link Map} where keys are {@link Player} instances, and values are {@link Pig} instances.
     */
    Map<Player, Pig> getVehiclesList();

    /**
     * Removes a specific vehicle from the box.
     *
     * @param p The {@link Player} whose vehicle should be removed.
     */
    void removeVehicle(Player p);

    /**
     * Gets the instance of this box.
     *
     * @return The {@link TBox} instance.
     */
    TBox getInstance();

    /**
     * Checks whether the box can be opened by clicking.
     *
     * @return {@code true} if the box can be opened by clicking, otherwise {@code false}.
     */
    boolean isClickToOpen();

    /**
     * Sets whether the box is currently in the opening state.
     *
     * @param state {@code true} if the box is opening, {@code false} otherwise.
     */
    void setOpeningState(Boolean state);

    /**
     * Checks whether the box is in the opening state.
     *
     * @return {@code true} if the box is opening, otherwise {@code false}.
     */
    boolean isOpening();

    /**
     * Gets the player's location.
     *
     * @return The {@link Location} of the player.
     */
    Location getPlayerLocation();

    /**
     * Gets the unique identifier of the box.
     *
     * @return The box ID as a {@link String}.
     */
    String getBoxId();

    /**
     * Gets the configuration manager of the box.
     *
     * @return The {@link TConfigManager} instance associated with this box.
     */
    TConfigManager getConfig();

    /**
     * Gets the possible locations where rewards can appear.
     *
     * @return A {@link List} of {@link Location} instances representing reward spawn points.
     */
    List<Location> getRewardsLocation();
}

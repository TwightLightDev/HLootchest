package org.twightlight.hlootchest.api.version_supports;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.functional.LootChestFactory;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NMSHandler {
    public static String owner;

    public final Plugin plugin;

    public NMSHandler(Plugin plugin, String versionName) {
        owner = versionName;
        this.plugin = plugin;
    }

    /**
     * Retrieves the NMS (Net Minecraft Server) service instance.
     *
     * @return The {@link NMSService} instance.
     */
    public abstract NMSService getNMSService();

    /**
     * Registers a button click event for a player.
     *
     * @param player The {@link Player} who clicked the button.
     */
    public abstract void registerButtonClick(Player player);

    /**
     * Spawns a button at the specified location.
     *
     * @param location The {@link Location} where the button should be spawned.
     * @param typePlayer The {@link ButtonType} of the button.
     * @param player The {@link Player} associated with the button.
     * @param icon The {@link ItemStack} representing the button's appearance.
     * @param path The configuration path related to the button.
     * @param config The {@link TConfigManager} instance managing configurations.
     */
    public abstract void spawnButton(Location location, ButtonType typePlayer, Player player, ItemStack icon, String path, TConfigManager config);

    /**
     * Spawns a loot box at the given location.
     *
     * @param location The {@link Location} where the box should be spawned.
     * @param boxid The unique ID of the loot box.
     * @param player The {@link Player} associated with the box.
     * @param icon The {@link ItemStack} representing the box's appearance.
     * @param config The {@link TConfigManager} instance managing configurations.
     * @return The spawned {@link TBox} instance.
     */
    public abstract TBox spawnBox(Location location, String boxid, Player player, ItemStack icon, TConfigManager config);

    /**
     * Removes all buttons of a specific type that belong to a player.
     *
     * @param player The {@link Player} whose buttons should be removed.
     * @param type The {@link ButtonType} of the buttons to remove.
     */
    public abstract void removeButtonsFromPlayer(Player player, ButtonType type);

    /**
     * Toggles the visibility of a player's buttons.
     *
     * @param player The {@link Player} whose buttons should be hidden or shown.
     * @param type The {@link ButtonType} of the buttons.
     * @param state {@code true} to hide the buttons, {@code false} to show them.
     */
    public abstract void hideButtonsFromPlayer(Player player, ButtonType type, boolean state);

    /**
     * Retrieves all globally registered buttons.
     *
     * @return A {@link ConcurrentHashMap} mapping {@link Player} instances to their corresponding list of {@link TButton} instances.
     */
    public abstract ConcurrentHashMap<Player, List<TButton>> getGlobalButtons();

    /**
     * Retrieves a button instance by its unique ID.
     *
     * @param id The unique ID of the button.
     * @return The corresponding {@link TButton} instance, or {@code null} if not found.
     */
    public abstract TButton getButtonFromId(int id);

    /**
     * Retrieves the box associated with a specific player.
     *
     * @param player The {@link Player} whose box is being retrieved.
     * @return The corresponding {@link TBox} instance, or {@code null} if not found.
     */
    public abstract TBox getBoxFromPlayer(Player player);

    /**
     * Creates an {@link ItemStack} with the specified material, amount, and data value.
     *
     * @param material The material of the item.
     * @param amount The quantity of the item.
     * @param data The data value of the item.
     * @return The created {@link ItemStack}.
     */
    public abstract ItemStack createItemStack(String material, int amount, short data);

    /**
     * Registers a loot chest factory for a given box ID.
     *
     * @param boxid The unique ID of the loot chest.
     * @param function The {@link LootChestFactory} instance associated with the box ID.
     */
    public abstract void register(String boxid, LootChestFactory function);

    /**
     * Retrieves all registered loot chest data.
     *
     * @return A {@link Map} mapping loot chest IDs to their respective {@link LootChestFactory} instances.
     */
    public abstract Map<String, LootChestFactory> getRegistrationData();

    /**
     * Converts a string representation of a location into a {@link Location} instance.
     *
     * @param locString The string representation of the location.
     * @return The corresponding {@link Location} instance.
     */
    public abstract Location stringToLocation(String locString);

    /**
     * Sets a player's game mode without actually changing it server-side.
     *
     * @param p The {@link Player} whose fake game mode is being set.
     * @param gamemode The {@link GameMode} to set.
     */
    public abstract void setFakeGameMode(Player p, GameMode gamemode);

    /**
     * Plays a sound at a specific location for a player.
     *
     * @param player The {@link Player} who will hear the sound.
     * @param location The {@link Location} where the sound originates.
     * @param sound The name of the sound to play.
     * @param yaw The yaw rotation of the sound.
     * @param pitch The pitch of the sound.
     */
    public abstract void playSound(Player player, Location location, String sound, float yaw, float pitch);

    /**
     * Creates an item with various properties, including material, head texture, display name, and lore.
     *
     * @param material The {@link Material} of the item.
     * @param headUrl The URL of a custom player head texture (if applicable).
     * @param data The data value of the item.
     * @param displayName The display name of the item.
     * @param lore The lore text associated with the item.
     * @param enchanted Whether the item should have a glowing effect.
     * @return The created {@link ItemStack}.
     */
    public abstract ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted);
}

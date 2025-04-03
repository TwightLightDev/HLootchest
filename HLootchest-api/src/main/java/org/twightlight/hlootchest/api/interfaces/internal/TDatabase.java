package org.twightlight.hlootchest.api.interfaces.internal;

import com.google.common.reflect.TypeToken;
import org.bukkit.OfflinePlayer;
import org.twightlight.hlootchest.api.enums.DatabaseType;

import java.sql.Connection;
import java.util.Map;

public interface TDatabase {
    /**
     * Retrieves the active database connection.
     *
     * @return The {@link Connection} to the database.
     */
    Connection getConnection();

    /**
     * Creates a new entry for the given player in the database.
     *
     * @param p The {@link OfflinePlayer} whose data should be initialized.
     */
    void createPlayerData(OfflinePlayer p);

    /**
     * Retrieves the type of the database in use.
     *
     * @return The {@link DatabaseType} representing the database type.
     */
    DatabaseType getDatabaseType();

    /**
     * Retrieves loot chest data for a given player and column.
     *
     * @param player1 The {@link OfflinePlayer} whose data is being retrieved.
     * @param column1 The column name to fetch data from.
     * @param type The {@link TypeToken} specifying the expected data type.
     * @param fallback The fallback value to return if the data is not found.
     * @param <T> The type of the returned data.
     * @return The data from the specified column, or the fallback value if not found.
     */
    <T> T getLootChestData(OfflinePlayer player1, String column1, TypeToken<T> type, T fallback);

    /**
     * Retrieves a mapping of loot chest IDs to their respective amounts for a given player.
     *
     * @param player The {@link OfflinePlayer} whose loot chest data is being retrieved.
     * @param column The column name to fetch data from.
     * @return A {@link Map} containing loot chest IDs as keys and their amounts as values.
     */
    Map<String, Integer> getLootChestData(OfflinePlayer player, String column);

    /**
     * Adds a loot chest entry for the specified player.
     *
     * @param player The {@link OfflinePlayer} whose loot chest data is being updated.
     * @param lootchestId The ID of the loot chest.
     * @param amount The amount of the loot chest to add.
     * @param column The column where the data should be stored.
     * @return {@code true} if the operation was successful, otherwise {@code false}.
     */
    boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column);

    /**
     * Pulls data from the database and assigns it to the specified column for a player.
     *
     * @param player The {@link OfflinePlayer} whose data is being updated.
     * @param data The data to store.
     * @param column The column where the data should be stored.
     * @param <T> The type of the data being stored.
     * @return {@code true} if the operation was successful, otherwise {@code false}.
     */
    <T> boolean pullData(OfflinePlayer player, T data, String column);

    /**
     * Adds a new column to the database if it does not already exist.
     *
     * @param columnName The name of the column to add.
     * @param columnType The type of the column (e.g., VARCHAR, INT).
     * @param defaultValue The default value to assign to the column.
     * @return {@code true} if the column was added successfully, otherwise {@code false}.
     */
    boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue);
}

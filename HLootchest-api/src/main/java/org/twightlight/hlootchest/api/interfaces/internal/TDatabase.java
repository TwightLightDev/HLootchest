package org.twightlight.hlootchest.api.interfaces.internal;

import com.google.common.reflect.TypeToken;
import org.bukkit.OfflinePlayer;
import org.twightlight.hlootchest.api.enums.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TDatabase {
    // Connection Management
    Connection getConnection() throws SQLException;
    boolean isConnected();

    // Player Data Operations
    void createPlayerData(OfflinePlayer player);
    DatabaseType getDatabaseType();

    // Data Retrieval
    <T> T getLootChestData(OfflinePlayer player, String column, TypeToken<T> type, T fallback);
    Map<String, Integer> getLootChestData(OfflinePlayer player, String column);

    // Data Modification
    <T> boolean updateData(OfflinePlayer player, T data, String column);
    boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column);

    // Schema Operations
    boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue);

    // Utility Methods
    void shutdown();

    // Async
    CompletableFuture<Void> createPlayerDataAsync(OfflinePlayer player);

    <T> CompletableFuture<T> getLootChestDataAsync(OfflinePlayer player, String column, TypeToken<T> type, T fallback);

    CompletableFuture<Map<String, Integer>> getLootChestDataAsync(OfflinePlayer player, String column);

    <T> CompletableFuture<Boolean> updateDataAsync(OfflinePlayer player, T data, String column);

    CompletableFuture<Boolean> addLootchestAsync(OfflinePlayer player, String lootchestId, Integer amount, String column);
}

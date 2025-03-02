package org.twightlight.hlootchest.api.interfaces.internal;

import com.google.common.reflect.TypeToken;
import org.bukkit.OfflinePlayer;
import org.twightlight.hlootchest.api.enums.DatabaseType;

import java.sql.Connection;
import java.util.Map;

public interface TDatabase {
    Connection getConnection();

    void createPlayerData(OfflinePlayer p);

    DatabaseType getDatabaseType();
    <T> T getLootChestData(OfflinePlayer player1, String column1, TypeToken<T> type, T fallback);

    Map<String, Integer> getLootChestData(OfflinePlayer player, String column);
    boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column);
    <T> boolean pullData(OfflinePlayer player, T data, String column);
    boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue);
}

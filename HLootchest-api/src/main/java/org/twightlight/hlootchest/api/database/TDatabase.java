package org.twightlight.hlootchest.api.database;

import com.google.common.reflect.TypeToken;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.util.Map;

public interface TDatabase {
    Connection getConnection();

    void createPlayerData(OfflinePlayer p);

    DatabaseType getDatabaseType();
    <T> T getLootChestData(OfflinePlayer player1, String column1, TypeToken<T> type);

    Map<String, Integer> getLootChestData(OfflinePlayer player, String column);
    boolean addData(OfflinePlayer player, String lootchestId, Integer amount, String column);
    <T> boolean pullData(OfflinePlayer player, T data, String column);
}

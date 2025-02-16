package org.twightlight.hlootchest.api.database;

import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.util.Map;

public interface TDatabase {
    Connection getConnection();

    void createPlayerData(OfflinePlayer p);

    DatabaseType getDatabaseType();

    Map<String, Integer> getLootChestData(OfflinePlayer player, String column);
    boolean addData(OfflinePlayer player, String lootchestId, Integer amount, String column);
    boolean pullData(OfflinePlayer player, Map<String, Integer> data, String column);
}

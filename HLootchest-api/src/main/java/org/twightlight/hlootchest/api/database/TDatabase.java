package org.twightlight.hlootchest.api.database;

import org.bukkit.OfflinePlayer;

import java.sql.Connection;

public interface TDatabase {
    Connection getConnection();

    void createPlayerData(OfflinePlayer p);

    DatabaseType getDatabaseType();
}

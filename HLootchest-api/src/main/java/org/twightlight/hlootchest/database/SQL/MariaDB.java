package org.twightlight.hlootchest.database.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.classloader.LibsLoader;
import org.twightlight.hlootchest.database.SQLDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDB extends SQLDatabase {
    public MariaDB(String host, int port, String database,
                   String username, String password, boolean ssl) {
        super(DatabaseType.MARIADB, createDataSource(host, port, database, username, password, ssl));
    }

    private static HikariDataSource createDataSource(String host, int port, String database,
                                                     String username, String password, boolean useSSL) {

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", host, port, database));
            config.setUsername(username);
            config.setPassword(password);
            config.setPoolName("HLootChest-MariaDB-Pool");

            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            if (useSSL) {
                config.addDataSourceProperty("sslMode", "REQUIRED");
            }

            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hlootchest (" +
                    "player VARCHAR(36) PRIMARY KEY, " +
                    "lootchests TEXT DEFAULT '{}', " +
                    "opened TEXT DEFAULT '{}', " +
                    "fallback_loc TEXT DEFAULT '', " +
                    "awaiting_rewards TEXT DEFAULT NULL) " +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize MariaDB database", e);
        }
    }
}
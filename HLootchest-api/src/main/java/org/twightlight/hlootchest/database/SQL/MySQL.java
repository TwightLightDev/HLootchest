package org.twightlight.hlootchest.database.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.classloader.LibsLoader;
import org.twightlight.hlootchest.database.SQLDatabase;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL extends SQLDatabase {
    public MySQL(LibsLoader libsLoader, String host, int port, String database,
                 String username, String password, boolean ssl) {
        super(DatabaseType.MYSQL, createDataSource(libsLoader, host, port, database, username, password, ssl));
    }

    private static HikariDataSource createDataSource(LibsLoader libsLoader, String host, int port, String database,
                                                     String username, String password, boolean useSSL) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(libsLoader);
            HikariConfig config = new HikariConfig();
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&verifyServerCertificate=%b&requireSSL=%b",
                    host, port, database, useSSL, useSSL, useSSL);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setPoolName("HLootChest-MySQL-Pool");

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
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
                    "awaiting_rewards TEXT DEFAULT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize MySQL database", e);
        }
    }
}

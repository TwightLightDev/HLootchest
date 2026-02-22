package org.twightlight.hlootchest.database.SQL;

import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.database.SQLDatabase;
import org.twightlight.hlootchest.dependency.ClassLoader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL extends SQLDatabase {

    public MySQL(ClassLoader classloader, String host, int port, String database,
                 String username, String password, boolean ssl) {
        super(DatabaseType.MYSQL, createDataSource(classloader, host, port, database, username, password, ssl));
    }

    private static Object createDataSource(java.lang.ClassLoader libLoader, String host, int port,
                                           String database, String username, String password, boolean useSSL) {
        java.lang.ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(libLoader);
            Class.forName("com.mysql.cj.jdbc.Driver", true, libLoader);

            Class<?> hikariConfigClass = Class.forName("com.zaxxer.hikari.HikariConfig", true, libLoader);
            Class<?> hikariDataSourceClass = Class.forName("com.zaxxer.hikari.HikariDataSource", true, libLoader);

            Object config = hikariConfigClass.getDeclaredConstructor().newInstance();

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%b&verifyServerCertificate=%b&requireSSL=%b",
                    host, port, database, useSSL, useSSL, useSSL
            );

            hikariConfigClass.getMethod("setDriverClassName", String.class).invoke(config, "com.mysql.cj.jdbc.Driver");
            hikariConfigClass.getMethod("setJdbcUrl", String.class).invoke(config, jdbcUrl);
            hikariConfigClass.getMethod("setUsername", String.class).invoke(config, username);
            hikariConfigClass.getMethod("setPassword", String.class).invoke(config, password);
            hikariConfigClass.getMethod("setPoolName", String.class).invoke(config, "HLootChest-MySQL-Pool");
            hikariConfigClass.getMethod("setMaximumPoolSize", int.class).invoke(config, 10);
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, Object.class).invoke(config, "cachePrepStmts", "true");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, Object.class).invoke(config, "prepStmtCacheSize", "250");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, Object.class).invoke(config, "prepStmtCacheSqlLimit", "2048");

            return hikariDataSourceClass.getConstructor(hikariConfigClass).newInstance(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to init MySQL datasource", e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Override
    protected void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hlootchest (" +
                    "player VARCHAR(36) PRIMARY KEY, " +
                    "lootchests TEXT DEFAULT NULL, " +
                    "opened TEXT DEFAULT NULL, " +
                    "fallback_loc TEXT DEFAULT NULL, " +
                    "awaiting_rewards TEXT DEFAULT NULL) " +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize MySQL database", e);
        }
    }
}

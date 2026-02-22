package org.twightlight.hlootchest.database.SQL;

import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.database.SQLDatabase;
import org.twightlight.hlootchest.dependency.ClassLoader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends SQLDatabase {

    public SQLite(Plugin plugin, ClassLoader classloader) {
        super(DatabaseType.SQLITE, createDataSource(plugin, classloader));
    }

    private static Object createDataSource(Plugin plugin, java.lang.ClassLoader libLoader) {
        File dataFile = new File(plugin.getDataFolder(), "hlootchest.db");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create SQLite database file", e);
            }
        }

        java.lang.ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(libLoader);

            Class<?> hikariConfigClass = Class.forName("com.zaxxer.hikari.HikariConfig", true, libLoader);
            Class<?> hikariDataSourceClass = Class.forName("com.zaxxer.hikari.HikariDataSource", true, libLoader);

            Object config = hikariConfigClass.newInstance();

            hikariConfigClass.getMethod("setDriverClassName", String.class)
                    .invoke(config, "org.sqlite.JDBC");
            hikariConfigClass.getMethod("setJdbcUrl", String.class)
                    .invoke(config, "jdbc:sqlite:" + dataFile.getAbsolutePath());
            hikariConfigClass.getMethod("setPoolName", String.class)
                    .invoke(config, "HLootChest-SQLite-Pool");
            hikariConfigClass.getMethod("setMaximumPoolSize", int.class)
                    .invoke(config, 1);
            hikariConfigClass.getMethod("setConnectionTestQuery", String.class)
                    .invoke(config, "SELECT 1");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, Object.class)
                    .invoke(config, "journal_mode", "WAL");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, Object.class)
                    .invoke(config, "synchronous", "NORMAL");

            return hikariDataSourceClass
                    .getConstructor(hikariConfigClass)
                    .newInstance(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HikariCP for SQLite", e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Override
    protected void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS hlootchest (" +
                    "player TEXT PRIMARY KEY, " +
                    "lootchests TEXT DEFAULT '{}', " +
                    "opened TEXT DEFAULT '{}', " +
                    "fallback_loc TEXT DEFAULT '', " +
                    "awaiting_rewards TEXT DEFAULT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }
}

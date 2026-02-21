package org.twightlight.hlootchest.database.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.database.SQLDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends SQLDatabase {

    public SQLite(Plugin plugin) {
        super(DatabaseType.SQLITE, createDataSource(plugin));
    }

    private static HikariDataSource createDataSource(Plugin plugin) {
        File dataFile = new File(plugin.getDataFolder(), "hlootchest.db");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create SQLite database file", e);
            }
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dataFile.getAbsolutePath());
        config.setPoolName("HLootChest-SQLite-Pool");
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");

        return new HikariDataSource(config);
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

    @Override
    public void createPlayerData(org.bukkit.OfflinePlayer p) {
        String query = "INSERT OR IGNORE INTO hlootchest (player, lootchests, opened, fallback_loc, awaiting_rewards) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, "{}");
            ps.setString(3, "{}");
            ps.setString(4, "\"\"");
            ps.setString(5, "NULL");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create player data", e);
        }
    }
}

package org.twightlight.hlootchest.database.SQL;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLite implements TDatabase {
    private Connection connection;
    private final Plugin plugin;
    private final Gson gson = new Gson();
    private final ExecutorService dbExecutor;
    private static final Logger LOGGER = Logger.getLogger("HLootChest-SQLite");

    public SQLite(Plugin plugin) {
        this.plugin = plugin;
        this.dbExecutor = Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r, "HLootChest-DB");
            t.setDaemon(true);
            return t;
        });
        connect();
    }

    public void connect() {
        LOGGER.info("&aConnecting to your database...");
        this.connection = getConnectionInternal();
        LOGGER.info("&aConnected successfully to your database!");
        LOGGER.info("&aCreating tables...");
        try (Statement statement = getConnectionInternal().createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS hlootchest (" +
                            "player TEXT PRIMARY KEY, " +
                            "lootchests TEXT DEFAULT '{}', " +
                            "opened TEXT DEFAULT '{}', " +
                            "fallback_loc TEXT DEFAULT '', " +
                            "awaiting_rewards TEXT DEFAULT NULL" +
                            ");"
            );
            LOGGER.info("Tables created successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnectionInternal() {
        File dataFile = new File(plugin.getDataFolder().getPath(), "hlootchest.db");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            if (this.connection != null && !this.connection.isClosed())
                return this.connection;
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            return this.connection;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnectionInternal();
    }

    @Override
    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void createPlayerData(OfflinePlayer p) {
        try (PreparedStatement ps = getConnectionInternal().prepareStatement(
                "INSERT OR IGNORE INTO hlootchest (player, lootchests, opened, fallback_loc, awaiting_rewards) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, "{}");
            ps.setString(3, "{}");
            ps.setString(4, "");
            ps.setString(5, null);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create player data for " + p.getUniqueId(), e);
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }

    @Override
    public <T> T getLootChestData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        String query = "SELECT " + column + " FROM hlootchest WHERE player = ?";
        try (PreparedStatement ps = getConnectionInternal().prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString(column);
                if (json == null || json.equals("NULL") || json.isEmpty()) {
                    return fallback;
                }
                try {
                    T result = gson.fromJson(json, typeToken.getType());
                    return result != null ? result : fallback;
                } catch (JsonSyntaxException e) {
                    LOGGER.warning("Invalid JSON in database for " + column + ": " + json);
                    return fallback;
                }
            }
            return fallback;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get loot chest data for " + player.getUniqueId() + " column=" + column, e);
            return fallback;
        }
    }

    @Override
    public Map<String, Integer> getLootChestData(OfflinePlayer player, String column) {
        return getLootChestData(player, column, new TypeToken<Map<String, Integer>>() {}, new HashMap<>());
    }

    @Override
    public <T> boolean updateData(OfflinePlayer player, T data, String column) {
        String query = "UPDATE hlootchest SET " + column + " = ? WHERE player = ?";
        try (PreparedStatement ps = getConnectionInternal().prepareStatement(query)) {
            ps.setString(1, data == null ? null : gson.toJson(data));
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update data for " + player.getUniqueId() + " column=" + column, e);
            return false;
        }
    }

    @Override
    public boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        Map<String, Integer> lchs = getLootChestData(player, column);
        if (!lchs.containsKey(lootchestId)) {
            return false;
        }
        int newVal = lchs.get(lootchestId) + amount;
        if (newVal < 0) {
            return false;
        }
        lchs.put(lootchestId, newVal);
        return updateData(player, lchs, column);
    }

    @Override
    public boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue) {
        try {
            Connection conn = getConnectionInternal();
            if (!columnExists(columnName)) {
                String statement = "ALTER TABLE hlootchest ADD COLUMN " + columnName + " " + columnType +
                        (defaultValue != null ? " DEFAULT " + defaultValue : "");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(statement);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to add column " + columnName, e);
            return false;
        }
    }

    private boolean columnExists(String columnName) throws SQLException {
        try (PreparedStatement pstmt = getConnectionInternal().prepareStatement("PRAGMA table_info(hlootchest)");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        dbExecutor.shutdown();
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException ignored) {}
    }

    @Override
    public CompletableFuture<Void> createPlayerDataAsync(OfflinePlayer player) {
        return CompletableFuture.runAsync(() -> createPlayerData(player), dbExecutor);
    }

    @Override
    public <T> CompletableFuture<T> getLootChestDataAsync(OfflinePlayer player, String column, TypeToken<T> type, T fallback) {
        return CompletableFuture.supplyAsync(() -> getLootChestData(player, column, type, fallback), dbExecutor);
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getLootChestDataAsync(OfflinePlayer player, String column) {
        return CompletableFuture.supplyAsync(() -> getLootChestData(player, column), dbExecutor);
    }

    @Override
    public <T> CompletableFuture<Boolean> updateDataAsync(OfflinePlayer player, T data, String column) {
        return CompletableFuture.supplyAsync(() -> updateData(player, data, column), dbExecutor);
    }

    @Override
    public CompletableFuture<Boolean> addLootchestAsync(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        return CompletableFuture.supplyAsync(() -> addLootchest(player, lootchestId, amount, column), dbExecutor);
    }
}

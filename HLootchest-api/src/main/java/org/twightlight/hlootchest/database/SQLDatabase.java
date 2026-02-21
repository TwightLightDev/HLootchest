package org.twightlight.hlootchest.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SQLDatabase implements TDatabase {

    protected final Object dataSource;
    protected final Gson gson = new Gson();
    protected final DatabaseType type;
    protected final ExecutorService dbExecutor;
    private static final Logger LOGGER = Logger.getLogger("HLootChest");

    protected SQLDatabase(DatabaseType type, Object dataSource) {
        this.type = type;
        this.dataSource = dataSource;
        this.dbExecutor = Executors.newFixedThreadPool(
                type == DatabaseType.SQLITE ? 1 : 4,
                r -> {
                    Thread t = new Thread(r, "HLootChest-DB");
                    t.setDaemon(true);
                    return t;
                }
        );
        initializeDatabase();
    }

    protected abstract void initializeDatabase();

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return (Connection) dataSource
                    .getClass()
                    .getMethod("getConnection")
                    .invoke(dataSource);
        } catch (Exception e) {
            throw new SQLException("Failed to get connection from datasource", e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return dataSource != null &&
                    !(boolean) dataSource
                            .getClass()
                            .getMethod("isClosed")
                            .invoke(dataSource);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return type;
    }

    protected String insertIgnoreSyntax() {
        if (type == DatabaseType.SQLITE) {
            return "INSERT OR IGNORE INTO";
        }
        return "INSERT IGNORE INTO";
    }

    @Override
    public void createPlayerData(OfflinePlayer p) {
        String query = insertIgnoreSyntax() + " hlootchest (player, lootchests, opened, fallback_loc, awaiting_rewards) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
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
    public <T> T getLootChestData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        String query = "SELECT " + column + " FROM hlootchest WHERE player = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString(column);
                if (json == null || json.equals("NULL") || json.isEmpty()) {
                    return fallback;
                }
                T result = gson.fromJson(json, typeToken.getType());
                return result != null ? result : fallback;
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
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
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
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String selectQuery = "SELECT " + column + " FROM hlootchest WHERE player = ?";
            Map<String, Integer> lchs;
            try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String json = rs.getString(column);
                    lchs = gson.fromJson(json, new TypeToken<Map<String, Integer>>() {}.getType());
                    if (lchs == null) lchs = new HashMap<>();
                } else {
                    return false;
                }
            }

            if (!lchs.containsKey(lootchestId)) {
                conn.rollback();
                return false;
            }
            int newVal = lchs.get(lootchestId) + amount;
            if (newVal < 0) {
                conn.rollback();
                return false;
            }
            lchs.put(lootchestId, newVal);

            String updateQuery = "UPDATE hlootchest SET " + column + " = ? WHERE player = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, gson.toJson(lchs));
                ps.setString(2, player.getUniqueId().toString());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Failed to add lootchest for " + player.getUniqueId(), e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue) {
        try (Connection conn = getConnection()) {
            if (!columnExists(conn, columnName)) {
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

    protected boolean columnExists(Connection conn, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, "hlootchest", columnName)) {
            return rs.next();
        }
    }

    @Override
    public void shutdown() {
        dbExecutor.shutdown();
        if (dataSource == null) return;
        try {
            boolean closed = (boolean) dataSource
                    .getClass()
                    .getMethod("isClosed")
                    .invoke(dataSource);
            if (!closed) {
                dataSource.getClass()
                        .getMethod("close")
                        .invoke(dataSource);
            }
        } catch (Exception ignored) {}
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

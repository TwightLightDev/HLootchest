package org.twightlight.hlootchest.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLDatabase implements TDatabase {
    protected final Object dataSource;
    protected final Gson gson = new Gson();
    protected final DatabaseType type;

    protected SQLDatabase(DatabaseType type, Object dataSource){
        this.type = type;
        this.dataSource = dataSource;
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

    @Override
    public void createPlayerData(OfflinePlayer p) {
        String query = "INSERT IGNORE INTO hlootchest (player, lootchests, opened, fallback_loc, awaiting_rewards) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
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

    @Override
    public <T> T getLootChestData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        String query = "SELECT " + column + " FROM hlootchest WHERE player = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = rs.getString(column);
                return gson.fromJson(json, typeToken.getType());
            }
            return fallback;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get loot chest data", e);
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
            ps.setString(1, gson.toJson(data));
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update data", e);
        }
    }

    @Override
    public boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        Map<String, Integer> lchs = getLootChestData(player, column);
        if (!lchs.containsKey(lootchestId)) {
            return false;
        }
        if (lchs.get(lootchestId) + amount < 0) {
            return false;
        }
        lchs.put(lootchestId, lchs.get(lootchestId) + amount);
        return updateData(player, lchs, column);
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
        } catch (Exception ignored) {
        }
    }
}

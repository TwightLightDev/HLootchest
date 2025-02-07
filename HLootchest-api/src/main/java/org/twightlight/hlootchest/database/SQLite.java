package org.twightlight.hlootchest.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.database.DatabaseType;
import org.twightlight.hlootchest.api.database.TDatabase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLite implements TDatabase {
    private Connection connection;
    private Plugin plugin;
    private DatabaseType type;

    public SQLite(Plugin plugin, DatabaseType type) {
        this.plugin = plugin;
        this.type = type;
        connect();
    }

    public void connect() {
        System.out.println("[HLootchest] " + "Connecting to your database...");
        this.connection = getConnection();
        System.out.println("[HLootchest] " + "Connected successfully to your database!");
        System.out.println("[HLootchest] " + "Creating tables...");
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(" CREATE TABLE IF NOT EXISTS hlootchest ( player TEXT PRIMARY KEY, lootchests TEXT DEFAULT '{}', opened TEXT DEFAULT '{}'); ");
            statement.close();
            System.out.println("[HLootchest] " + "Tables created successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        File dataFile = new File(plugin.getDataFolder().getPath(), "hlootchest.db");
        if (!dataFile.exists())
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    public void createPlayerData(OfflinePlayer p) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM hlootchest WHERE player = '" + p.getUniqueId().toString() + "'");
            ResultSet rs = ps.executeQuery();
            String player = null;
            if (rs.next())
                player = rs.getString("player");
            if (player != null)
                return;
            connection = getConnection();
            ps = connection.prepareStatement("INSERT INTO hlootchest (player, lootchests, opened) VALUES (?, ?, ?)");
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, "{}");
            ps.setString(3, "{}");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public DatabaseType getDatabaseType() {
        return this.type;
    }

    public Map<String, Integer> getLootChestData(OfflinePlayer player, String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT " + column + " FROM hlootchest WHERE player = ?");
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            Map<String, Integer> lootchests = null;
            Gson gson = new Gson();
            if (rs.next()) {
                String lootchestsString = rs.getString(column);
                Type type = new TypeToken<Map<String, Integer>>() {
                }.getType();
                lootchests = gson.fromJson(lootchestsString, type);
            }
            if (lootchests != null)
                return lootchests;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    public boolean pullData(OfflinePlayer player, Map<String, Integer> data, String column) {
        try {
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE hlootchest SET " + column + "=? WHERE player=?");
            Gson gson = new Gson();
            ps.setString(1, gson.toJson(data));
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            ps.close();
            c.close();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new NullPointerException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean addData(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        Map<String, Integer> lchs = getLootChestData(player, column);
        if (!lchs.containsKey(lootchestId)) {
            return false;
        }
        if (lchs.get(lootchestId) + amount < 0) {
            return false;
        }
        lchs.put(lootchestId, lchs.get(lootchestId) + amount);
        return pullData(player, lchs, column);
    }
}


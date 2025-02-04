package org.twightlight.hlootchest.database;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.database.DatabaseType;
import org.twightlight.hlootchest.api.database.TDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.*;

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
            statement.executeUpdate(" CREATE TABLE IF NOT EXISTS hlootchest ( player TEXT PRIMARY KEY, lootchests TEXT DEFAULT '{}'); ");
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
            ps = connection.prepareStatement("INSERT INTO hlootchest (player, lootchests) VALUES (?, ?)");
            ps.setString(1, p.getUniqueId().toString());
            ps.setString(2, "{}");
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
}


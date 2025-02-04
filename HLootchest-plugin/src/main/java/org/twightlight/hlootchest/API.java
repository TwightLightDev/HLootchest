package org.twightlight.hlootchest;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.database.TDatabase;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.sessions.LootChestSessions;
import org.twightlight.hlootchest.utils.Utility;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class API implements HLootchest {
    private final HLootchest.ConfigUtil configUtil = new ConfigUtil();
    private final HLootchest.SessionUtil sessionUtil = new SessionUtil();
    private final HLootchest.DatabaseUtil dbUtil = new DatabaseUtil();


    private static class ConfigUtil implements HLootchest.ConfigUtil {

        public TConfigManager getTemplateConfig() {
            return org.twightlight.hlootchest.HLootchest.templateConfig;
        }
        public TConfigManager getMainConfig() {
            return org.twightlight.hlootchest.HLootchest.mainConfig;
        }
        public TConfigManager getBoxesConfig() {
            return org.twightlight.hlootchest.HLootchest.boxesConfig;
        }
        public TConfigManager getMessageConfig() { return org.twightlight.hlootchest.HLootchest.messagesConfig; }
    }

    private static class SessionUtil implements HLootchest.SessionUtil {

        public TSessions getSessionFromPlayer(Player p) {
            return LootChestSessions.sessions.get(p);
        }
    }

    private static class DatabaseUtil implements HLootchest.DatabaseUtil {
        public Map<String, Integer> getLootChestData(OfflinePlayer player) {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = getDb().getConnection();
                ps = conn.prepareStatement("SELECT lootchests FROM hlootchest WHERE player = ?");
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                Map<String, Integer> lootchests = null;
                Gson gson = new Gson();
                if (rs.next()) {
                    String lootchestsString = rs.getString("lootchests");
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

        public boolean pullData(OfflinePlayer player, Map<String, Integer> data) {
            try {
                Connection c = getDb().getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE hlootchest SET lootchests=? WHERE player=?");
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
                Utility.error("An error occurred while pulling data: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Utility.error("Invalid Argument: " + e.getMessage());
            }
            return false;
        }

        public boolean addLootChest(OfflinePlayer player, String lootchestId, Integer amount) {
            Map<String, Integer> lchs = getLootChestData(player);
            if (!lchs.containsKey(lootchestId)) {
                return false;
            }
            lchs.put(lootchestId, lchs.get(lootchestId) + amount);
            return pullData(player, lchs);
        }

        public TDatabase getDb() {
            return org.twightlight.hlootchest.HLootchest.db;
        }
    }

    public HLootchest.ConfigUtil getConfigUtil() {
        return configUtil;
    }
    public HLootchest.SessionUtil getSessionUtil() {
        return sessionUtil;
    }
    public HLootchest.DatabaseUtil getDatabaseUtil() {
        return dbUtil;
    }

}

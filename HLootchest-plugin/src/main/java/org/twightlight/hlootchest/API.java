package org.twightlight.hlootchest;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.sessions.LootChestSession;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Map;

public class API implements HLootchest {
    private final HLootchest.ConfigUtil configUtil = new ConfigUtil();
    private final HLootchest.SessionUtil sessionUtil = new SessionUtil();
    private final HLootchest.DatabaseUtil dbUtil = new DatabaseUtil();
    private final HLootchest.PlayerUtil playerUtil = new PlayerUtil();


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

        public TSession getSessionFromPlayer(Player p) {
            return LootChestSession.sessions.get(p);
        }
        public Map<Player, TSession> getSessionsList() {
            return LootChestSession.sessions;
        }
        public void closeAll() {
            for (Player p : getSessionsList().keySet()) {
                getSessionsList().get(p).close();
            }
        }
    }

    private static class DatabaseUtil implements HLootchest.DatabaseUtil {

        public TDatabase getDb() {
            return org.twightlight.hlootchest.HLootchest.db;
        }
    }

    private static class PlayerUtil implements HLootchest.PlayerUtil {
        public void addLootChest(Player p, String lc, int amount) {
            org.twightlight.hlootchest.HLootchest.getAPI().getDatabaseUtil().getDb().addLootchest(p, lc, amount, "lootchests");
        }
        public void newLcSession(Player p, String lc) {
            new LootChestSession(p, lc);
        }
        public void leaveLcSession(Player p) {
            TSession session = org.twightlight.hlootchest.HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
            if (session != null) {
                session.close();
            }
        }
        public boolean checkConditions(Player p, TConfigManager config, String path) {
            return Utility.checkConditions(p, config, path);
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
    public HLootchest.PlayerUtil getPlayerUtil() {
        return playerUtil;
    }
    public NMSHandler getNMS() { return org.twightlight.hlootchest.HLootchest.getNms(); }

}

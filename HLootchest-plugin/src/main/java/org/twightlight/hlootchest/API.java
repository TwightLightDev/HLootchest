package org.twightlight.hlootchest;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.sessions.LootChestSession;
import org.twightlight.hlootchest.supports.hooks.HeadDatabase;
import org.twightlight.hlootchest.supports.interfaces.HooksLoader;
import org.twightlight.hlootchest.utils.ActionHandler;
import org.twightlight.hlootchest.utils.Utility;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class API implements HLootchest {
    private final HLootchest.ConfigUtil configUtil = new ConfigUtil();
    private final HLootchest.SessionUtil sessionUtil = new SessionUtil();
    private final HLootchest.DatabaseUtil dbUtil = new DatabaseUtil();
    private final HLootchest.Debug debug = new Debug();
    private final HLootchest.LanguageUtil languageUtil = new LanguageUtil();
    private final HLootchest.PlayerUtil playerUtil = new PlayerUtil();


    private static class LanguageUtil implements HLootchest.LanguageUtil {
        public String getMsg(Player p, String path) {
            return p(p, org.twightlight.hlootchest.HLootchest.messagesConfig.getString("Messages." + path).replace("{prefix}", org.twightlight.hlootchest.HLootchest.messagesConfig.getString("Messages.prefix")));
        }

        public String pC(Player p, String value) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
                return c(value);
            return c(PlaceholderAPI.setPlaceholders(p, value));
        }

        public String p(Player p, String value) {
            if (org.twightlight.hlootchest.HLootchest.hex_gradient) {
                return org.twightlight.hlootchest.HLootchest.colorUtils.colorize(pC(p, value));
            }
            return pC(p, value);
        }

        public String c(String value) {
            return ChatColor.translateAlternateColorCodes('&', value);
        }

        public void sendHelp(Player p, String target) {
            List<String> helps = org.twightlight.hlootchest.HLootchest.messagesConfig.getList("Messages." + target + "-help");
            if (helps == null) {
                return;
            }
            for (String s : helps) {
                p.sendMessage(p(p, s));
            }
        }

        public String replaceCommand(Player p, String command) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
                return command.replace("{player}", p.getName());
            return PlaceholderAPI.setPlaceholders(p, command);
        }
    }

    private static class ConfigUtil implements HLootchest.ConfigUtil {

        public TConfigManager getTemplateConfig() {
            return org.twightlight.hlootchest.HLootchest.templateConfig;
        }
        public TConfigManager getMainConfig() {
            return org.twightlight.hlootchest.HLootchest.mainConfig;
        }
        public TConfigManager getBoxesConfig(String id) {
            return org.twightlight.hlootchest.HLootchest.boxesConfigMap.get(id);
        }
        public Map<String, TConfigManager> getBoxesConfigs() {
            return org.twightlight.hlootchest.HLootchest.boxesConfigMap;
        }
        public TConfigManager getMessageConfig() { return org.twightlight.hlootchest.HLootchest.messagesConfig; }
        public void registerConfig(String id, TConfigManager config) {
            org.twightlight.hlootchest.HLootchest.boxesConfigMap.put(id, config);
        }


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

        public ActionHandler getActionHandler() {
            return org.twightlight.hlootchest.HLootchest.getInstance().getActionHandler();
        }
    }

    private static class Debug implements HLootchest.Debug {
        public void sendDebugMsg(Player p, String msg) {

            if (isDebug()) {
                p.sendMessage("[Debug] " + msg);
            }
        }
        public boolean isDebug() {
            return org.twightlight.hlootchest.HLootchest.mainConfig.getBoolean("debug", false);
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
    public HooksLoader getHooksLoader() {
        return org.twightlight.hlootchest.HLootchest.getInstance().getHooksLoader();
    }
    public HLootchest.Debug getDebugService() {
        return debug;
    }
    public HLootchest.LanguageUtil getLanguageUtil() {
        return languageUtil;
    }
}

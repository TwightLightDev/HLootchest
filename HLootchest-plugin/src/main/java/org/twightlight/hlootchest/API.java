package org.twightlight.hlootchest;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.classloader.LibsLoader;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.database.DatabaseManager;
import org.twightlight.hlootchest.sessions.LootChestSession;
import org.twightlight.hlootchest.supports.interfaces.HooksLoader;
import org.twightlight.hlootchest.utils.ActionHandler;
import org.twightlight.hlootchest.utils.Utility;

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
            return p(p, ConfigManager.messagesConfig.getString("Messages." + path).replace("{prefix}", ConfigManager.messagesConfig.getString("Messages.prefix")));
        }

        public String pC(Player p, String value) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
                return c(value);
            return c(PlaceholderAPI.setPlaceholders(p, value));
        }

        public String p(Player p, String value) {
            if (HLootChest.hex_gradient) {
                return HLootChest.colorUtils.colorize(pC(p, value));
            }
            return pC(p, value);
        }

        public String c(String value) {
            return ChatColor.translateAlternateColorCodes('&', value);
        }

        public void sendHelp(Player p, String target) {
            List<String> helps = ConfigManager.messagesConfig.getList("Messages." + target + "-help");
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

        public TYamlWrapper getTemplateConfig() {
            return ConfigManager.templateConfig;
        }
        public TYamlWrapper getMainConfig() {
            return ConfigManager.mainConfig;
        }
        public TYamlWrapper getBoxesConfig(String id) {
            return ConfigManager.boxesConfigMap.get(id);
        }
        public Map<String, TYamlWrapper> getBoxesConfigs() {
            return ConfigManager.boxesConfigMap;
        }
        public TYamlWrapper getMessageConfig() { return ConfigManager.messagesConfig; }
        public void registerConfig(String id, TYamlWrapper config) {
            ConfigManager.boxesConfigMap.put(id, config);
        }

        public TYamlWrapper getRegistrationConfig() { return ConfigManager.registration; }

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

        public TDatabase getDatabase() {
            return HLootChest.db.getDatabase();
        }
        public DatabaseManager getDatabaseManager() {
            return HLootChest.db;
        }

    }

    private static class PlayerUtil implements HLootchest.PlayerUtil {


        public void addLootChest(Player p, String lc, int amount) {
            HLootChest.getAPI().getDatabaseUtil().getDatabase().addLootchest(p, lc, amount, "lootchests");
        }
        public void newLcSession(Player p, String lc) {
            new LootChestSession(p, lc);
        }
        public void leaveLcSession(Player p) {
            TSession session = HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p);
            if (session != null) {
                session.close();
            }
        }
        public boolean checkConditions(Player p, TYamlWrapper config, String path) {
            return Utility.checkConditions(p, config, path);
        }

        public ActionHandler getActionHandler() {
            return HLootChest.getInstance().getActionHandler();
        }
    }

    private static class Debug implements HLootchest.Debug {
        public void sendDebugMsg(Player p, String msg) {

            if (isDebug()) {
                p.sendMessage("[Debug] " + msg);
            }
        }
        public boolean isDebug() {
            return ConfigManager.mainConfig.getBoolean("debug", false);
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
    public NMSHandler getNMS() { return HLootChest.getNms(); }
    public HooksLoader getHooksLoader() {
        return HLootChest.getInstance().getHooksLoader();
    }

    @Override
    public LibsLoader getLibsLoader() {
        return HLootChest.getInstance().getLibsLoader();
    }

    public HLootchest.Debug getDebugService() {
        return debug;
    }
    public HLootchest.LanguageUtil getLanguageUtil() {
        return languageUtil;
    }
}

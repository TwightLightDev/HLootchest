package org.twightlight.hlootchest;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.sessions.TSessionManager;
import org.twightlight.hlootchest.sessions.SessionManager;

public class API {
    private final HLootchest.ConfigUtil configUtil = new ConfigUtil();
    private final HLootchest.SessionUtil sessionUtil = new SessionUtil();


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

        public TSessionManager getSessionFromPlayer(Player p) {
            if (SessionManager.sessionData.get(p) != null) {
                return SessionManager.sessionData.get(p);
            } else {
                return null;
            }
        }

    }

    public HLootchest.ConfigUtil getConfigUtil() {
        return configUtil;
    }
    public HLootchest.SessionUtil getSessionUtil() {return sessionUtil;}

}

package org.twightlight.hlootchest.api;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;

import java.util.Map;

public interface HLootchest {

    ConfigUtil getConfigUtil();
    SessionUtil getSessionUtil();
    DatabaseUtil getDatabaseUtil();
    PlayerUtil getPlayerUtil();
    NMSHandler getNMS();

    interface ConfigUtil {
        TConfigManager getTemplateConfig();
        TConfigManager getMainConfig();
        TConfigManager getBoxesConfig();
        TConfigManager getMessageConfig();
    }

    interface SessionUtil {
        TSession getSessionFromPlayer(Player p);
        Map<Player, TSession> getSessionsList();
        void closeAll();
    }

    interface DatabaseUtil {
        TDatabase getDb();
    }

    interface PlayerUtil {
        void addLootChest(Player p, String lc, int amount);

        void newLcSession(Player p, String lc);

        void leaveLcSession(Player p);

        boolean checkConditions(Player p, TConfigManager config, String path);
    }
}

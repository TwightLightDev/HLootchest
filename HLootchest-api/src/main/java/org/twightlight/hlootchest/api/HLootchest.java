package org.twightlight.hlootchest.api;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.database.TDatabase;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.api.supports.NMSHandler;

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
        TSessions getSessionFromPlayer(Player p);
        Map<Player, TSessions> getSessionsList();
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

package org.twightlight.hlootchest.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.database.TDatabase;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;

import java.util.Map;

public interface HLootchest {

    ConfigUtil getConfigUtil();
    SessionUtil getSessionUtil();
    DatabaseUtil getDatabaseUtil();

    interface ConfigUtil {
        TConfigManager getTemplateConfig();
        TConfigManager getMainConfig();
        TConfigManager getBoxesConfig();
        TConfigManager getMessageConfig();
    }

    interface SessionUtil {
        TSessions getSessionFromPlayer(Player p);
    }

    interface DatabaseUtil {
        Map<String, Integer> getLootChestData(OfflinePlayer player);
        boolean addLootChest(OfflinePlayer player, String lootchestId, Integer amount);
        boolean pullData(OfflinePlayer player, Map<String, Integer> data);
        TDatabase getDb();
    }

}

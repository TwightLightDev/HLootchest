package org.twightlight.hlootchest.api;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.supports.hooks.HeadDatabase;
import org.twightlight.hlootchest.supports.interfaces.HooksLoader;
import org.twightlight.hlootchest.utils.ActionHandler;

import java.util.Map;

public interface HLootchest {

    /**
     * Retrieves the configuration utility.
     *
     * @return The {@link ConfigUtil} instance.
     */
    ConfigUtil getConfigUtil();

    /**
     * Retrieves the session utility.
     *
     * @return The {@link SessionUtil} instance.
     */
    SessionUtil getSessionUtil();

    /**
     * Retrieves the database utility.
     *
     * @return The {@link DatabaseUtil} instance.
     */
    DatabaseUtil getDatabaseUtil();

    /**
     * Retrieves the player utility.
     *
     * @return The {@link PlayerUtil} instance.
     */
    PlayerUtil getPlayerUtil();

    /**
     * Retrieves the NMS handler.
     *
     * @return The {@link NMSHandler} instance.
     */
    NMSHandler getNMS();

    /**
     * Retrieves the debugging service.
     *
     * @return The {@link Debug} instance.
     */
    Debug getDebugService();

    /**
     * Retrieves the language utility.
     *
     * @return The {@link LanguageUtil} instance.
     */
    LanguageUtil getLanguageUtil();

    HooksLoader getHooksLoader();

    interface LanguageUtil {
        String getMsg(Player p, String path);

        String pC(Player p, String value);

        String p(Player p, String value);

        String c(String value);

        void sendHelp(Player p, String target);

        String replaceCommand(Player p, String command);
    }
    /**
     * Manages configuration files.
     */
    interface ConfigUtil {
        /**
         * Retrieves the template configuration file.
         *
         * @return The {@link TConfigManager} instance for the template configuration.
         */
        TConfigManager getTemplateConfig();

        /**
         * Retrieves the main configuration file.
         *
         * @return The {@link TConfigManager} instance for the main configuration.
         */
        TConfigManager getMainConfig();

        /**
         * Retrieves the boxes configuration file.
         *
         * @return The {@link TConfigManager} instance for the box configuration.
         */
        TConfigManager getBoxesConfig(String id);

        /**
         * Retrieves the boxes configuration file.
         *
         * @return The {@link Map} instance for the box configuration.
         */
        Map<String, TConfigManager> getBoxesConfigs();
        /**
         * Retrieves the messages configuration file.
         *
         * @return The {@link TConfigManager} instance for the message configuration.
         */
        TConfigManager getMessageConfig();
        /**
         * Registers a config file for a given box ID.
         *
         * @param id The unique ID of the loot chest.
         * @param config The {@link TConfigManager} you want to link with.
         */
        void registerConfig(String id, TConfigManager config);
    }

    /**
     * Manages player sessions.
     */
    interface SessionUtil {
        /**
         * Retrieves the session associated with a player.
         *
         * @param p The {@link Player}.
         * @return The corresponding {@link TSession}, or {@code null} if not found.
         */
        TSession getSessionFromPlayer(Player p);

        /**
         * Retrieves a list of all active sessions.
         *
         * @return A {@link Map} of players and their associated {@link TSession} instances.
         */
        Map<Player, TSession> getSessionsList();

        /**
         * Closes all active sessions.
         */
        void closeAll();
    }

    /**
     * Manages database interactions.
     */
    interface DatabaseUtil {
        /**
         * Retrieves the database instance.
         *
         * @return The {@link TDatabase} instance.
         */
        TDatabase getDb();
    }

    /**
     * Provides debugging utilities.
     */
    interface Debug {
        /**
         * Sends a debug message to a player.
         *
         * @param p The {@link Player} receiving the message.
         * @param msg The debug message to send.
         */
        void sendDebugMsg(Player p, String msg);

        boolean isDebug();
    }

    /**
     * Provides player-related utilities.
     */
    interface PlayerUtil {
        /**
         * Adds a loot chest to a player’s inventory.
         *
         * @param p The {@link Player} receiving the loot chest.
         * @param lc The loot chest identifier.
         * @param amount The number of loot chests to add.
         */
        void addLootChest(Player p, String lc, int amount);

        /**
         * Starts a new loot chest session for a player.
         *
         * @param p The {@link Player} starting the session.
         * @param lc The loot chest identifier.
         */
        void newLcSession(Player p, String lc);

        /**
         * Ends a loot chest session for a player.
         *
         * @param p The {@link Player} leaving the session.
         */
        void leaveLcSession(Player p);

        /**
         * Checks if a player meets certain conditions.
         *
         * @param p The {@link Player}.
         * @param config The {@link TConfigManager} configuration.
         * @param path The configuration path to check.
         * @return {@code true} if the conditions are met, otherwise {@code false}.
         */
        boolean checkConditions(Player p, TConfigManager config, String path);

        ActionHandler getActionHandler();
    }
}

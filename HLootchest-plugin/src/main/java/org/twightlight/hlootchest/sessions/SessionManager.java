package org.twightlight.hlootchest.sessions;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.SessionType;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.sessions.TSessionManager;

import java.util.HashMap;
import java.util.Map;

public class SessionManager implements TSessionManager {

    public static Player player;
    public static SessionType type;
    public static TConfigManager file;
    public static String name;

    public static Map<Player, TSessionManager> sessionData = new HashMap<>();

    public SessionManager(Player p, TConfigManager file1, String name1) {
        player = p;
        file = file1;
        name = name1;
    }

    public Player getPlayer() {
        return player;
    }

    public void save() {}

    public void close() {
        sessionData.remove(player);
    }
}



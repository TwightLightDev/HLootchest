package org.twightlight.hlootchest.sessions;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;

import java.util.HashMap;
import java.util.Map;

public class SessionsManager {
    public static final Map<Player, TSession> sessions = new HashMap<>();
}

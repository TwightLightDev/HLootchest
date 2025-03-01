package org.twightlight.hlootchest.sessions;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.TSessions;

import java.util.HashMap;
import java.util.Map;

public class SessionsManager {
    public static final Map<Player, TSessions> sessions = new HashMap<>();
}

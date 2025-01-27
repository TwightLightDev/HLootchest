package org.twightlight.hlootchest.sessions;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.objects.TConfigManager;

public class LootchestSession {
    private Player player;

    public LootchestSession(Player player, TConfigManager dataFile) {
        this.player = player;
    }
}

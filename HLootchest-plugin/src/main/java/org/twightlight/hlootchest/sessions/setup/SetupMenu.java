package org.twightlight.hlootchest.sessions.setup;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SetupMenu {

    private String boxid;
    private Player player;

    private Map<Integer, String> slotData = new HashMap<>();

    public void openMainSetup(Player player) {
        this.player = player;
    }

    public void openLootchestSetup(String boxid, Player player) {
        this.boxid = boxid;

    }

    public void openButtonSetup() {

    }
}

package org.twightlight.hlootchest.objects;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.enums.ButtonType;

import java.util.List;

public class Reward {

    private final List<String> actions;

    public Reward(List<String> actions) {
        this.actions = actions;
    }

    public void accept(Player p) {
        for (String action : actions) {
            HLootChest.getAPI().getPlayerUtil().getActionHandler().handle(action, p, ButtonType.REWARD);
        }
    }
}

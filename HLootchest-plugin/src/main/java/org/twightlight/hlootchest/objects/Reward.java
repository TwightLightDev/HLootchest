package org.twightlight.hlootchest.objects;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.Main;
import org.twightlight.hlootchest.utils.Utility;

import java.util.List;

public class Reward {

    private final List<String> actions;

    public Reward(List<String> actions) {
        this.actions = actions;
    }

    public void accept(Player p) {
        for (String action : actions) {
            HLootchest.getAPI().getPlayerUtil().getActionHandler().handle(action, p, ButtonType.REWARD);
        }
    }
}

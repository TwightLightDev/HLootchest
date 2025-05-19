package org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023.modules;


import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.events.player.PlayerXpGainEvent;
import org.bukkit.entity.Player;

public class Levels {

    BedWars api;

    public Levels(BedWars api) {
        this.api = api;
    }

    public void addXp(Player p, int amount) {
        api.getLevelsUtil().addXp(p, amount, PlayerXpGainEvent.XpSource.OTHER);
    }

    public void setXp(Player p, int amount) {
        api.getLevelsUtil().setXp(p, amount);
    }

    public void setLevel(Player p, int level) {
        api.getLevelsUtil().setLevel(p, level);
    }

    public void addLevel(Player p, int level) {
        int current_level = api.getLevelsUtil().getPlayerLevel(p);
        api.getLevelsUtil().setLevel(p, level + current_level);
    }
}

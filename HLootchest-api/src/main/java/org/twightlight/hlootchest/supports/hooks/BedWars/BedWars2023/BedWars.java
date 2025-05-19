package org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023;

import org.bukkit.Bukkit;
import org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023.modules.Levels;

public class BedWars {
    private final com.tomkeuper.bedwars.api.BedWars api;
    private final Levels levelsUtil;

    public BedWars() {
        api = Bukkit.getServicesManager().getRegistration(com.tomkeuper.bedwars.api.BedWars.class).getProvider();
        levelsUtil = new Levels(api);
    }

    public com.tomkeuper.bedwars.api.BedWars getBedWarsAPI() {
        return api;
    }

    public Levels getLevelsUtil() {
        return levelsUtil;
    }
}

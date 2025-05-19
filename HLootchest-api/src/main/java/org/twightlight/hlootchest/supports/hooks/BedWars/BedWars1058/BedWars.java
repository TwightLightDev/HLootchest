package org.twightlight.hlootchest.supports.hooks.BedWars.BedWars1058;

import org.bukkit.Bukkit;
import org.twightlight.hlootchest.supports.hooks.BedWars.BedWars1058.modules.Levels;

public class BedWars {
    private final com.andrei1058.bedwars.api.BedWars api;
    private final Levels levelsUtil;

    public BedWars() {
        api = Bukkit.getServicesManager().getRegistration(com.andrei1058.bedwars.api.BedWars.class).getProvider();
        levelsUtil = new Levels(api);
    }

    public com.andrei1058.bedwars.api.BedWars getBedWarsAPI() {
        return api;
    }

    public Levels getLevelsUtil() {
        return levelsUtil;
    }
}

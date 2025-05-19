package org.twightlight.hlootchest.supports.bstats;

import org.bukkit.Bukkit;
import org.twightlight.hlootchest.HLootchest;

public class bStats {
    public static void init() {
        int pluginId = 24836;
        Metrics metrics = new Metrics(HLootchest.getInstance(), pluginId);

        metrics.addCustomChart(new Metrics.SingleLineChart("players_online", () -> Bukkit.getOnlinePlayers().size()));

        metrics.addCustomChart(new Metrics.SingleLineChart("servers", () -> 1));
    }
}

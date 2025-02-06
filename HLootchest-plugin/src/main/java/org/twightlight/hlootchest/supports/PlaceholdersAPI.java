package org.twightlight.hlootchest.supports;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.API;
import org.twightlight.hlootchest.HLootchest;

public class PlaceholdersAPI extends PlaceholderExpansion {
    private final HLootchest plugin;

    public PlaceholdersAPI(HLootchest pl) {
        this.plugin = pl;
    }

    @Override
    public String getIdentifier() {
        return "hlootchest";
    }

    @Override
    public String getAuthor() {
        return "twightlightdev";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String placeholder) {
        HLootchest.getInstance();
        API api = HLootchest.getAPI();
        switch (placeholder.toLowerCase()) {
            case "total":
                int sum = api.getDatabaseUtil().getDb().getLootChestData(player, "lootchests")
                        .values().stream().mapToInt(Integer::intValue).sum();
                return String.valueOf(sum);

            default:
                String[] args = placeholder.toLowerCase().split("_");
                switch (args[0]) {
                    case "current":
                        return String.valueOf(api.getDatabaseUtil().getDb().getLootChestData(player, "lootchests").getOrDefault(args[1], -1));
                    case "opened":
                        return String.valueOf(api.getDatabaseUtil().getDb().getLootChestData(player, "opened").getOrDefault(args[1], -1));
                }
        }
        return null;
    }
}

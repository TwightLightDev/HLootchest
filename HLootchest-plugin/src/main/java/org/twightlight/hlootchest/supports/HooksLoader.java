package org.twightlight.hlootchest.supports;

import org.bukkit.Bukkit;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.supports.hooks.BedWars.BedWars1058.BedWars;
import org.twightlight.hlootchest.supports.interfaces.PlaceholderAPI;
import org.twightlight.hlootchest.utils.Utility;

public class HooksLoader implements org.twightlight.hlootchest.supports.interfaces.HooksLoader {
    private final PlaceholderAPI placeholdersAPI;
    private final org.twightlight.hlootchest.supports.interfaces.HeadDatabase headDatabase;
    private final org.twightlight.hlootchest.supports.interfaces.BedWars1058 bw1058;
    private final org.twightlight.hlootchest.supports.interfaces.BedWars2023 bw2023;

    public HooksLoader() {
        Utility.info("§6§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Utility.info("            §aStarting dependencies loader...");
        placeholdersAPI = new PlaceholdersAPI();
        headDatabase = new HeadDatabase();
        bw1058 = new BedWars1058();
        bw2023 = new BedWars2023();
        if (!bw1058.hasBedWars() && !bw2023.hasBedWars()) {
            Utility.info("§cBedWars not found! Disabling BedWars integration...");
        } else if (bw1058.hasBedWars() && bw2023.hasBedWars()) {
            Utility.error("§cBoth BedWars1058 and BedWars2023 found! Please delete one");
            Utility.error("§cDisabling plugin due to error...");
            Bukkit.getPluginManager().disablePlugin(HLootChest.getInstance());
        } else {
            Utility.info("§7BedWars: §aEnabled");
        }
        Utility.info("§6§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    }

    private static class PlaceholdersAPI implements PlaceholderAPI {
        private boolean papi = false;

        public PlaceholdersAPI() {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new org.twightlight.hlootchest.supports.hooks.PlaceholdersAPI(HLootChest.getInstance()).register();
                papi = true;
                Utility.info("§aPlaceholderAPI found! Starting PlaceholderAPI integration...");
                Utility.info("§7PlaceholderAPI: §aEnabled");
            } else {
                Utility.info("§cPlaceholderAPI not found! Disabling PlaceholderAPI integration...");
            }
        }

        public boolean hasPapi() {
            return papi;
        }
    }

    private static class HeadDatabase implements org.twightlight.hlootchest.supports.interfaces.HeadDatabase {
        private boolean hasHeadDb = false;
        private org.twightlight.hlootchest.supports.hooks.HeadDatabase headDb;

        public HeadDatabase() {
            if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
                headDb = new org.twightlight.hlootchest.supports.hooks.HeadDatabase();
                hasHeadDb = true;
                Utility.info("§aHeadDatabase found! Starting HeadDatabase integration...");
                Utility.info("§7HeadDatabase: §aEnabled");
            } else {
                Utility.info("§cHeadDatabase not found! Disabling HeadDatabase integration...");
            }
        }

        public boolean hasHeadDatabase() {
            return hasHeadDb;
        }

        public org.twightlight.hlootchest.supports.hooks.HeadDatabase getHeadDatabaseService() {
            return headDb;
        }
    }

    private static class BedWars1058 implements org.twightlight.hlootchest.supports.interfaces.BedWars1058 {
        private boolean hasbw = false;
        private BedWars bw1058;

        public BedWars1058() {
            if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
                bw1058 = new BedWars();
                hasbw = true;
                Utility.info("§aBedWars1058 found! Starting BedWars1058 integration...");

            }
        }

        public boolean hasBedWars() {
            return hasbw;
        }

        public BedWars getBedWarsService() {
            return bw1058;
        }
    }

    private static class BedWars2023 implements org.twightlight.hlootchest.supports.interfaces.BedWars2023 {
        private boolean hasbw = false;
        private org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023.BedWars bw2023;

        public BedWars2023() {
            if (Bukkit.getPluginManager().getPlugin("BedWars2023") != null) {
                bw2023 = new org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023.BedWars();
                hasbw = true;
                Utility.info("§aBedWars2023 found! Starting BedWars2023 integration...");
            }
        }

        public boolean hasBedWars() {
            return hasbw;
        }

        public org.twightlight.hlootchest.supports.hooks.BedWars.BedWars2023.BedWars getBedWarsService() {
            return bw2023;
        }
    }

    public PlaceholderAPI getPlaceholdersAPIHook() {
        return placeholdersAPI;
    }

    public org.twightlight.hlootchest.supports.interfaces.HeadDatabase getHeadDatabaseHook() {
        return headDatabase;
    }

    public org.twightlight.hlootchest.supports.interfaces.BedWars1058 getBedWars1058Hook() {
        return bw1058;
    }

    public org.twightlight.hlootchest.supports.interfaces.BedWars2023 getBedWars2023Hook() {
        return bw2023;
    }
}

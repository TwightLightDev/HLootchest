package org.twightlight.hlootchest;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.commands.admin.AdminCommand;
import org.twightlight.hlootchest.commands.admin.AdminTabCompleter;
import org.twightlight.hlootchest.commands.main.MainCommands;
import org.twightlight.hlootchest.commands.main.MainTabCompleter;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.config.configs.MainConfig;
import org.twightlight.hlootchest.database.SQLite;
import org.twightlight.hlootchest.listeners.LootChests;
import org.twightlight.hlootchest.listeners.PlayerJoin;
import org.twightlight.hlootchest.listeners.PlayerQuit;
import org.twightlight.hlootchest.listeners.Setup;
import org.twightlight.hlootchest.supports.HeadDatabase;
import org.twightlight.hlootchest.supports.PlaceholdersAPI;
import org.twightlight.hlootchest.utils.ColorUtils;
import org.twightlight.hlootchest.utils.Metrics;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.hlootchest.utils.VersionChecker;

import java.io.File;

public final class HLootchest extends JavaPlugin {

    private static NMSHandler nms;
    private static API api;
    private String path = getDataFolder().getPath();
    private boolean papi = false;
    private boolean hasHeadDb = false;
    public static HeadDatabase headDb;
    public static TConfigManager mainConfig;
    public static TConfigManager templateConfig;
    public static TConfigManager boxesConfig;
    public static TConfigManager messagesConfig;
    public static TDatabase db;
    public static boolean hex_gradient = false;
    public static ColorUtils colorUtils;
    private static final String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1];

    @Override
    public void onEnable() {
        api = new API();
        Bukkit.getServicesManager().register(org.twightlight.hlootchest.api.HLootchest.class, api, this, ServicePriority.Normal);
        loadNMS();
        loadCommands();
        loadListeners();
        loadConf();
        loadDatabase();
        loadDependencies();
        loadCredit();
        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().init();
        }
        if (mainConfig.getBoolean("metrics")) {
            loadMetrics();
        }
        new VersionChecker(this, "122671").checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.getAPI().terminate();
        }
        api.getSessionUtil().closeAll();
        Bukkit.getScheduler().cancelTasks(this);
        Utility.info("HLootChest has been disabled successfully!");
        Utility.info("Current Version: " + version);
    }

    private void loadNMS() {
        switch (version) {
            case "8":
                nms = new org.twightlight.hlootchest.supports.v1_8_R3.Main(this, version, api);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular::new);
                nms.register("mystic", org.twightlight.hlootchest.supports.v1_8_R3.boxes.Mystic::new);
                nms.register("spooky", org.twightlight.hlootchest.supports.v1_8_R3.boxes.Spooky::new);

                break;
            case "12":
                nms = new org.twightlight.hlootchest.supports.v1_12_R1.Main(this, version, api);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_12_R1.boxes.Regular::new);
                nms.register("mystic", org.twightlight.hlootchest.supports.v1_12_R1.boxes.Mystic::new);
                nms.register("spooky", org.twightlight.hlootchest.supports.v1_12_R1.boxes.Spooky::new);

                break;
            case "19":
                nms = new org.twightlight.hlootchest.supports.v1_19_R3.Main(this, version, api);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Regular::new);
                nms.register("mystic", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Mystic::new);
                nms.register("spooky", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Spooky::new);
                break;
            case "20":
                String minor = Bukkit.getBukkitVersion().split("-")[0].split("\\.").length > 2 ? Bukkit.getBukkitVersion().split("-")[0].split("\\.")[2] : "0";
                if (Integer.parseInt(minor) <= 4) {
                    nms = new org.twightlight.hlootchest.supports.v1_20_R3.Main(this, version, api);
                    nms.register("regular", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Regular::new);
                    nms.register("mystic", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Mystic::new);
                    nms.register("spooky", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Spooky::new);
                    break;
                } else {
                    nms = new org.twightlight.hlootchest.supports.v1_20_R4.Main(this, version, api);
                    nms.register("regular", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Regular::new);
                    nms.register("mystic", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Mystic::new);
                    nms.register("spooky", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Spooky::new);
                    break;
                }
            case "21":
                nms = new org.twightlight.hlootchest.supports.v1_21_R3.Main(this, version, api);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Regular::new);
                nms.register("mystic", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Mystic::new);
                nms.register("spooky", org.twightlight.hlootchest.supports.v1_19_R3.boxes.Spooky::new);
                break;
            default:
                Utility.info("Sorry, this version is unsupported! HLootChest will be disable!");
                Bukkit.getPluginManager().disablePlugin(this);
        }

        if (Integer.parseInt(version) >= 16) {
            hex_gradient = true;
            colorUtils = new ColorUtils();
        }
    }
    private void loadCommands() {
        getCommand("hlootchests").setExecutor(new MainCommands());
        getCommand("hlootchests").setTabCompleter(new MainTabCompleter());
        getCommand("hlootchestsadmin").setExecutor(new AdminCommand());
        getCommand("hlootchestsadmin").setTabCompleter(new AdminTabCompleter());
    }

    private void loadListeners() {

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new LootChests(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new Setup(), HLootchest.getInstance());

    }

    private void loadConf() {
        Utility.info("Loading config.yml...");
        mainConfig = new MainConfig(this, "config", path);

        Utility.info("Loading templates...");
        File file = new File((getDataFolder().getPath()+ "/templates"), "example_template.yml");
        if (!file.exists()) {
            saveResource("templates/example_template.yml", false);
        }
        templateConfig = new ConfigManager(this, mainConfig.getString("template"), getDataFolder().getPath()+ "/templates");

        Utility.info("Loading lootchests...");
        File file2 = new File(getDataFolder().getPath(), "lootchests.yml");
        if (!file2.exists()) {
            saveResource("lootchests.yml", false);
        }
        boxesConfig = new ConfigManager(this, "lootchests", path);

        Utility.info("Loading messages.yml...");
        File file4 = new File(getDataFolder().getPath(), "messages.yml");
        if (!file4.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = new ConfigManager(this, "messages", path);

    }

    private void loadDatabase() {
        Utility.info("Connecting to database...");
        String provider = mainConfig.getString("storage.source");
        switch (provider) {
            case "SQLite":
                Utility.info("Using SQLite as database provider...");
                db = new SQLite(this, DatabaseType.SQLITE);
        }
        Utility.info("Your database is ready!");
    }

    private void loadCredit() {
        Utility.info("§6§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Utility.info("            §eHLootchest by §bTwightLight");
        Utility.info("  §7Github: §9https://github.com/TwightLightDev/HLootchest");
        Utility.info("  §7Minecraft Version: §a" + Bukkit.getBukkitVersion());
        Utility.info("  §7Plugin Version: §a" + getVersion());
        Utility.info("  §7Author: §a" + String.join(", ", getDescription().getAuthors()));
        Utility.info("  §7PlaceholderAPI: " + (isPlaceholderAPI() ? "§aEnabled" : "§cDisabled"));
        Utility.info("  §7HeadDatabase: " + (hasHeadDb() ? "§aEnabled" : "§cDisabled"));
        if (version.equals("19") || version.equals("20") || version.equals("21")) {
            Utility.info("  §7PacketEvents: " + (Bukkit.getPluginManager().getPlugin("packetevents") != null ? "§aEnabled" : "§cDisabled"));
        }
        Utility.info("  §7Hex & Gradient: " + (isHexGradient() ? "§aSupported" : "§cNot Supported"));
        Utility.info("§6§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void loadDependencies() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholdersAPI(this).register();
            papi = true;
        }
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            headDb = new HeadDatabase();
            hasHeadDb = true;
        }
    }

    private void loadMetrics() {
        int pluginId = 24836;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SingleLineChart("players_online", () -> Bukkit.getOnlinePlayers().size()));

        metrics.addCustomChart(new Metrics.SingleLineChart("servers", () -> 1));
    }

    public static NMSHandler getNms() {
        return nms;
    }

    public static HLootchest getInstance() {
        return getPlugin(HLootchest.class);
    }

    public static API getAPI() {
        return api;
    }

    public static String getFilePath() {
        return getInstance().getDataFolder().getPath();
    }

    public boolean isPlaceholderAPI() {
        return papi;
    }

    public boolean isHexGradient() {
        return hex_gradient;
    }

    public boolean hasHeadDb() {
        return hasHeadDb;
    }

    public static String getVersion() {
        return "1.0.9";
    }

    public static String getAPIVersion() {
        return version;
    }

}

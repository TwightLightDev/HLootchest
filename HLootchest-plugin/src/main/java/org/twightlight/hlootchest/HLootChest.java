package org.twightlight.hlootchest;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.dependency.*;
import org.twightlight.hlootchest.commands.admin.AdminCommand;
import org.twightlight.hlootchest.commands.admin.AdminTabCompleter;
import org.twightlight.hlootchest.commands.main.MainCommands;
import org.twightlight.hlootchest.commands.main.MainTabCompleter;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.config.YamlWrapper;
import org.twightlight.hlootchest.database.DatabaseManager;
import org.twightlight.hlootchest.database.RedisDatabase;
import org.twightlight.hlootchest.database.SQL.MariaDB;
import org.twightlight.hlootchest.database.SQL.MySQL;
import org.twightlight.hlootchest.database.SQL.SQLite;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.dependency.ClassLoader;
import org.twightlight.hlootchest.listeners.LootChests;
import org.twightlight.hlootchest.listeners.PlayerJoin;
import org.twightlight.hlootchest.listeners.PlayerQuit;
import org.twightlight.hlootchest.listeners.Setup;
import org.twightlight.hlootchest.scheduler.SchedulerAdapter;
import org.twightlight.hlootchest.scheduler.SchedulerProvider;
import org.twightlight.hlootchest.supports.HooksLoader;
import org.twightlight.hlootchest.supports.bstats.bStats;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.Main;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.boxes.Aeternus;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.boxes.Mystic;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.boxes.Regular;
import org.twightlight.hlootchest.supports.protocol.v1_8_R3.boxes.Spooky;
import org.twightlight.hlootchest.utils.*;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

public final class HLootChest extends JavaPlugin {

    private static NMSHandler nms;
    private static API api;
    private org.twightlight.hlootchest.supports.interfaces.HooksLoader hooksLoader;
    public static DatabaseManager db;
    public static boolean hex_gradient = false;
    public static ColorUtils colorUtils;
    private static final String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1];
    private ActionHandler actionHandler;
    private ClassLoader libsLoader;
    private static SchedulerAdapter scheduler;

    @Override
    public void onEnable() {
        Utility.setPlugin(this);
        scheduler = new SchedulerProvider(this).get();
        if (SchedulerProvider.isFolia()) {
            Utility.info("Folia detected! Using region-based scheduling.");
        }

        if (Bukkit.getPluginManager().getPlugin("TwightLightCore") == null) {
            Utility.info("TwightLightCore not found, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        api = new API();
        Bukkit.getServicesManager().register(org.twightlight.hlootchest.api.HLootchest.class, api, this, ServicePriority.Normal);
        ConfigManager.init();

        File d = new File(getFilePath() + "/libs");
        if (!d.exists()) {
            d.mkdirs();
        }
        Utility.info("Loading internal libraries...");

        DownloadManager downloader = new DownloadManager(
                getDataFolder().toPath().resolve("libs"),
                Repository.MAVEN_CENTRAL,
                getClassLoader()
        );

        try {
            libsLoader = CLBuilder.build(
                    new File(getDataFolder(), "libs"),
                    getClassLoader()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        loadNMS();
        loadLootchests();
        loadCommands();
        loadListeners();
        loadDatabase(downloader);
        actionHandler = new ActionHandler(HLootChest.getAPI(), scheduler);
        hooksLoader = new HooksLoader();
        loadCredit();

        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().init();
        }

        if (api.getConfigUtil().getMainConfig().getBoolean("metrics")) {
            bStats.init();
        }

        new VersionChecker(this, "122671").checkForUpdates();
        Utility.info("HLootChest has successfully been enabled!");

    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            try {
                PacketEvents.getAPI().terminate();
            } catch (Exception ignored) {}
        }

        if (api != null) {
            try {
                api.getSessionUtil().closeAll();
            } catch (Exception ignored) {}
        }

        scheduler.cancelAll();

        if (db != null && db.getDatabase() != null) {
            try {
                db.getDatabase().shutdown();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error shutting down database", e);
            }
        }

        Utility.info("HLootChest has been disabled successfully!");
    }

    private void loadNMS() {
        switch (version) {
            case "8":
                nms = new Main(this, version, api);
                nms.registerAnimation("regular", Regular::new);
                nms.registerAnimation("mystic", Mystic::new);
                nms.registerAnimation("spooky", Spooky::new);
                nms.registerAnimation("aeternus", Aeternus::new);
                break;
            case "12":
                nms = new org.twightlight.hlootchest.supports.protocol.v1_12_R1.Main(this, version, api);
                nms.registerAnimation("regular", org.twightlight.hlootchest.supports.protocol.v1_12_R1.boxes.Regular::new);
                nms.registerAnimation("mystic", org.twightlight.hlootchest.supports.protocol.v1_12_R1.boxes.Mystic::new);
                nms.registerAnimation("spooky", org.twightlight.hlootchest.supports.protocol.v1_12_R1.boxes.Spooky::new);
                nms.registerAnimation("aeternus", org.twightlight.hlootchest.supports.protocol.v1_12_R1.boxes.Aeternus::new);
                break;
            case "19":
                nms = new org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main(this, version, api);
                nms.registerAnimation("regular", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Regular::new);
                nms.registerAnimation("mystic", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Mystic::new);
                nms.registerAnimation("spooky", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Spooky::new);
                nms.registerAnimation("aeternus", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Aeternus::new);
                break;
            case "20":
                String minor = Bukkit.getBukkitVersion().split("-")[0].split("\\.").length > 2
                        ? Bukkit.getBukkitVersion().split("-")[0].split("\\.")[2] : "0";
                if (Integer.parseInt(minor) <= 4) {
                    nms = new org.twightlight.hlootchest.supports.protocol.v1_20_R3.Main(this, version, api);
                } else {
                    nms = new org.twightlight.hlootchest.supports.protocol.v1_20_R4.Main(this, version, api);
                }
                nms.registerAnimation("regular", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Regular::new);
                nms.registerAnimation("mystic", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Mystic::new);
                nms.registerAnimation("spooky", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Spooky::new);
                nms.registerAnimation("aeternus", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Aeternus::new);
                break;
            case "21":
                nms = new org.twightlight.hlootchest.supports.protocol.v1_21_R3.Main(this, version, api);
                nms.registerAnimation("regular", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Regular::new);
                nms.registerAnimation("mystic", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Mystic::new);
                nms.registerAnimation("spooky", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Spooky::new);
                nms.registerAnimation("aeternus", org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.Aeternus::new);
                break;
            default:
                Utility.info("Sorry, this version is unsupported! HLootChest will be disabled!");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
        }

        if (Integer.parseInt(version) >= 16) {
            hex_gradient = true;
            colorUtils = new ColorUtils();
        }
    }

    private void loadLootchests() {
        Set<String> types = api.getConfigUtil().getRegistrationConfig().getYml().getConfigurationSection("").getKeys(false);
        for (String type : types) {
            Utility.info("Registering type: " + type + ".");
            String animation = api.getConfigUtil().getRegistrationConfig().getString(type + ".animation", "regular");
            try {
                nms.register(type, api.getNMS().getAnimationsRegistrationData().get(animation));
            } catch (Exception e) {
                Utility.info("Something went wrong while registering the " + type + " box type");
                Utility.info("Diagnostic: The animation cannot be found!");
            }
            try {
                TYamlWrapper boxConfig = new YamlWrapper(this, type, getDataFolder().getPath() + "/lootchests");
                api.getConfigUtil().registerConfig(type, boxConfig);
                Utility.info("Matched " + type + " with " + type + ".yml");
            } catch (Exception e) {
                nms.deregister(type);
                Utility.info("Something went wrong while matching " + type + " to its config");
                Utility.info("You should rename the file you want to match to " + type + ".yml");
                Utility.info("Deregistering: " + type);
            }
        }
    }

    private void loadCommands() {
        getCommand("hlootchests").setExecutor(new MainCommands());
        getCommand("hlootchests").setTabCompleter(new MainTabCompleter());
        getCommand("hlootchestsadmin").setExecutor(new AdminCommand());
        getCommand("hlootchestsadmin").setTabCompleter(new AdminTabCompleter());
    }

    private void loadListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), HLootChest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(), HLootChest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new LootChests(), HLootChest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new Setup(), HLootChest.getInstance());
    }

    private void loadDatabase(DownloadManager downloader) {
        Utility.info("Connecting to database...");

        TYamlWrapper config = api.getConfigUtil().getMainConfig();
        db = new DatabaseManager();

        String provider = config.getString("database.provider", "sqlite").toLowerCase();

        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 3306);
        String database = config.getString("database.database", "twightlight");
        String username = config.getString("database.username", "root");
        String password = config.getString("database.password", "");
        boolean ssl = config.getBoolean("database.ssl", false);

        try {
            downloader.load(
                    new Dependency("com.zaxxer", "HikariCP", "5.1.0", false),
                    new Dependency("org.slf4j", "slf4j-api", "2.0.9", false),
                    new Dependency("org.slf4j", "slf4j-simple", "2.0.9", false)
            );

            TDatabase sqlDatabase;

            switch (provider) {

                case "mysql":
                    Utility.info("Using MySQL as database provider...");
                    downloader.load(
                            new Dependency("com.mysql", "mysql-connector-j", "8.3.0", false)
                    );
                    sqlDatabase = new MySQL(libsLoader, host, port, database, username, password, ssl);
                    break;

                case "mariadb":
                    Utility.info("Using MariaDB as database provider...");
                    downloader.load(
                            new Dependency("org.mariadb.jdbc", "mariadb-java-client", "3.3.2", false)
                    );
                    sqlDatabase = new MariaDB(libsLoader, host, port, database, username, password, ssl);
                    break;

                case "sqlite":
                default:
                    if (!provider.equals("sqlite")) {
                        Utility.info("Unknown database provider '" + provider + "', falling back to SQLite.");
                    }
                    Utility.info("Using SQLite as database provider...");
                    downloader.load(
                            new Dependency("org.xerial", "sqlite-jdbc", "3.45.1.0", false)
                    );
                    sqlDatabase = new SQLite(this, libsLoader);
                    break;
            }

            if (config.getBoolean("database.redis.enabled", false)) {

                downloader.load(
                        new Dependency("redis.clients", "jedis", "5.1.0", false),
                        new Dependency("org.apache.commons", "commons-pool2", "2.12.0", false)
                );

                String redisHost = config.getString("database.redis.host", "localhost");
                int redisPort = config.getInt("database.redis.port", 6379);
                String redisPassword = config.getString("database.redis.password", "");

                try {
                    sqlDatabase = new RedisDatabase(
                            sqlDatabase,
                            libsLoader,
                            redisHost,
                            redisPort,
                            redisPassword
                    );
                    Utility.info("Redis caching layer enabled!");
                } catch (Exception e) {
                    getLogger().log(Level.WARNING,
                            "Failed to initialize Redis, falling back to SQL only",
                            e);
                }
            }

            db.setDatabase(sqlDatabase);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database dependencies", e);
        }

        Utility.info("Your database is ready!");
    }

    private void loadCredit() {
        Utility.info("6m");
        Utility.info("            eHLootchest by bTwightLight");
        Utility.info("  7Github: 9https://github.com/TwightLightDev/HLootchest");
        Utility.info("  7Minecraft Version: a" + Bukkit.getBukkitVersion());
        Utility.info("  7Plugin Version: a" + getVersion());
        Utility.info("  7Author: a" + String.join(", ", getDescription().getAuthors()));
        Utility.info("  7Folia: " + (SchedulerProvider.isFolia() ? "aDetected" : "cNot Detected"));
        if (version.equals("19") || version.equals("20") || version.equals("21")) {
            Utility.info("  7PacketEvents: " + (Bukkit.getPluginManager().getPlugin("packetevents") != null ? "aEnabled" : "cDisabled"));
        }
        Utility.info("  7Hex & Gradient: " + (isHexGradient() ? "aSupported" : "cNot Supported"));
        Utility.info("6m");
    }

    public static NMSHandler getNms() { return nms; }
    public static HLootChest getInstance() { return getPlugin(HLootChest.class); }
    public static API getAPI() { return api; }
    public static String getFilePath() { return getInstance().getDataFolder().getPath(); }
    public boolean isHexGradient() { return hex_gradient; }
    public static String getVersion() { return "1.3.0"; }
    public static String getAPIVersion() { return version; }
    public org.twightlight.hlootchest.supports.interfaces.HooksLoader getHooksLoader() { return hooksLoader; }
    public ActionHandler getActionHandler() {
        return actionHandler;
    }
    public ClassLoader getLibsLoader() { return libsLoader; }
    public static SchedulerAdapter getScheduler() {
        return scheduler;
    }
}

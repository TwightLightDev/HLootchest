package org.twightlight.hlootchest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.hlootchest.api.database.DatabaseType;
import org.twightlight.hlootchest.api.database.TDatabase;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.commands.AdminCommand;
import org.twightlight.hlootchest.commands.MainCommands;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.config.configs.MainConfig;
import org.twightlight.hlootchest.database.SQLite;
import org.twightlight.hlootchest.listeners.LootChests;
import org.twightlight.hlootchest.listeners.PlayerJoin;
import org.twightlight.hlootchest.listeners.PlayerQuit;
import org.twightlight.hlootchest.supports.PlaceholdersAPI;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;

public final class HLootchest extends JavaPlugin {

    private static NMSHandler nms;
    private static API api;
    private String path = getDataFolder().getPath();
    private boolean papi = false;
    public static TConfigManager mainConfig;
    public static TConfigManager templateConfig;
    public static TConfigManager boxesConfig;
    public static TConfigManager messagesConfig;
    public static TDatabase db;
    private static final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];


    @Override
    public void onEnable() {
        api = new API();
        loadNMS();
        loadCommands();
        loadListeners();
        loadConf();
        loadDatabase();
        loadPlaceholdersAPI();
        loadCredit();
    }

    @Override
    public void onDisable() {
    }

    private void loadNMS() {
        switch (version) {
            case "v1_8_R3":
                nms = new Main(this, version);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular::new);
                break;
            case "v1_12_R1":
                nms = new org.twightlight.hlootchest.supports.v1_12_R1.Main(this, version);
                nms.register("regular", org.twightlight.hlootchest.supports.v1_12_R1.boxes.Regular::new);
                break;
        }
    }
    private void loadCommands() {
        this.getCommand("hlootchests").setExecutor(new MainCommands());
        this.getCommand("hlootchestsadmin").setExecutor(new AdminCommand());

    }
    private void loadListeners() {

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new LootChests(), HLootchest.getInstance());

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
        Utility.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Utility.info("HLootchest by TwightLight");
        Utility.info("Github Link: https://github.com/TwightLightDev/HLootchest");
        Utility.info("Minecraft Version: " + version);
        Utility.info("Plugin Version: " + getVersion());
        Utility.info("Author: " + getDescription().getAuthors().toString());
        Utility.info("PlaceholderAPI: " + isPlaceholderAPI());
        Utility.info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void loadPlaceholdersAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholdersAPI(this).register();
            papi = true;
        }
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

    public static String getVersion() {
        return "1.0.0";
    }

}

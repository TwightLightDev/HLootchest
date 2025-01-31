package org.twightlight.hlootchest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.commands.MainCommands;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.config.configs.BoxesConfig;
import org.twightlight.hlootchest.config.configs.MainConfig;
import org.twightlight.hlootchest.config.configs.MessageConfig;
import org.twightlight.hlootchest.listeners.*;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;

public final class HLootchest extends JavaPlugin {

    private static NMSHandler nms;
    private static API api;
    private String path = getDataFolder().getPath();
    public static TConfigManager mainConfig;
    public static TConfigManager templateConfig;
    public static TConfigManager boxesConfig;
    public static TConfigManager messagesConfig;
    private static final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

    @Override
    public void onEnable() {
        api = new API();
        loadNMS();
        loadCommands();
        loadListeners();
        loadConf();
    }

    @Override
    public void onDisable() {
    }

    private void loadNMS() {
        switch (version) {
            case "v1_8_R3":
                nms = new org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3(this, version);
                nms.register("regular", Regular::new);
        }
    }
    private void loadCommands() {
        this.getCommand("hlootchests").setExecutor(new MainCommands());
    }
    private void loadListeners() {

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new DamageEvent(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new DismountEvent(), HLootchest.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(), HLootchest.getInstance());

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
        boxesConfig = new BoxesConfig(this, "lootchests", path);
        Utility.info("Loading messages.yml...");
        messagesConfig = new MessageConfig(this, "messages", path);

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

}

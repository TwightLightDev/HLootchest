package org.twightlight.hlootchest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.commands.MainCommands;
import org.twightlight.hlootchest.config.BoxesConfig;
import org.twightlight.hlootchest.config.MainConfig;
import org.twightlight.hlootchest.config.TemplateConfig;
import org.twightlight.hlootchest.listeners.PlayerJoin;
import org.twightlight.hlootchest.utils.Utility;

public final class HLootchest extends JavaPlugin {

    private static NMSHandler nms;
    private static API api;
    public static TConfigManager mainConfig;
    public static TConfigManager templateConfig;
    public static TConfigManager boxesConfig;
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
        }
    }
    private void loadCommands() {
        this.getCommand("hlootchests").setExecutor(new MainCommands());
    }
    private void loadListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), HLootchest.getInstance());

    }

    private void loadConf() {
        Utility.info("Starting HLootchest...");
        String path = getDataFolder().getPath();
        Utility.info("Loading config.yml...");
        mainConfig = new MainConfig(this, "config", path);
        Utility.info("Loading templates...");
        templateConfig = new TemplateConfig(this, mainConfig.getString("template"), path+"/templates");
        Utility.info("Loading lootchests...");
        boxesConfig = new BoxesConfig(this, "lootchests", path);

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
}

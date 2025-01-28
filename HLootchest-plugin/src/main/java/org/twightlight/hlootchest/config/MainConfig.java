package org.twightlight.hlootchest.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.config.configs.ConfigManager;

public class MainConfig extends ConfigManager {
    public MainConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);
        YamlConfiguration yml = getYml();
        yml.options().header("HLootchest by TwightLightDev");

        yml.addDefault("debug", false);
        yml.addDefault("template", "example_template");

        yml.options().copyDefaults(true);
        save();
    }
}

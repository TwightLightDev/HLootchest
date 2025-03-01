package org.twightlight.hlootchest.config.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.config.ConfigManager;

import java.util.Arrays;

public class MainConfig extends ConfigManager {
    public MainConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);
        YamlConfiguration yml = getYml();
        yml.addDefault("debug", false);
        yml.addDefault("template", "example_template");

        yml.addDefault("storage.source", "SQLite");

        yml.addDefault("allowed-commands.opening", Arrays.asList("lc leave"));
        yml.addDefault("allowed-commands.setup", Arrays.asList("lc edit", "lc leave"));


        yml.options().copyDefaults(true);
        save();
    }
}

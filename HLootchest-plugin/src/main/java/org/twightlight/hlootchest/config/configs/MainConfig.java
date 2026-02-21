package org.twightlight.hlootchest.config.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.config.YamlWrapper;

import java.util.Arrays;

public class MainConfig extends YamlWrapper {
    public MainConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);
        YamlConfiguration yml = getYml();
        yml.addDefault("debug", false);
        yml.addDefault("metrics", true);
        yml.addDefault("template", "example_template");

        yml.addDefault("database.provider", "SQLite");

        yml.addDefault("database.host", "localhost");
        yml.addDefault("database.port", 3306);
        yml.addDefault("database.database", "twightlight");
        yml.addDefault("database.user", "root");
        yml.addDefault("database.pass", "");
        yml.addDefault("database.ssl", false);


        yml.addDefault("allowed-commands.opening", Arrays.asList("lc leave"));
        yml.addDefault("allowed-commands.setup", Arrays.asList("lc edit", "lc leave"));

        yml.addDefault("performance.modern-check-algorithm", false);

        yml.options().copyDefaults(true);
        save();
    }
}

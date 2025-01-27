package org.twightlight.hlootchest.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class TemplateConfig extends ConfigManager{
    public TemplateConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);
        YamlConfiguration yml = getYml();
        yml.options().header("HLootchest by TwightLightDev");


        yml.options().copyDefaults(true);
        save();
    }
}

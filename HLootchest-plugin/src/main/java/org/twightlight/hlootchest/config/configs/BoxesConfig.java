package org.twightlight.hlootchest.config.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class BoxesConfig extends ConfigManager {
    public BoxesConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);
        YamlConfiguration yml = getYml();
        yml.options().header("HLootchest by TwightLightDev");

        yml.addDefault("regular.icon.material", "SKULL_ITEM");
        yml.addDefault("regular.icon.data", 3);
        yml.addDefault("regular.icon.head_value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjE1ZmZmOGQ4ZThkYWRjMjZkZjQ3MWI2YzYxOTI2N2RmZDI3NDY3MjNjYzIyNWM3YjNiYmYyZjExZTlhYTJiOSJ9fX0");

        yml.options().copyDefaults(true);
        save();
    }
}

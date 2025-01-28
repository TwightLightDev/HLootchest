package org.twightlight.hlootchest.config.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class MessageConfig extends ConfigManager{
    public MessageConfig(Plugin pl, String name, String dir) {
        super(pl, name, dir);

        add("prefix", "&e[HLootchest]");

        add("noPerms", "{prefix} &cYou don't have enough permission!");
        add("noConsole", "{prefix} &cThis command can only be executed by player!");
        add("lootchestNotFound", "{prefix} &cInvalid Lootchest!");

        getYml().options().copyDefaults(true);
        save();
    }

    public void add(String path, String message) {
        getYml().addDefault("Messages."+path, message);
    }
}

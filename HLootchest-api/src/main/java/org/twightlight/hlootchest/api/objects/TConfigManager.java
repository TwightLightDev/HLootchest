package org.twightlight.hlootchest.api.objects;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public interface TConfigManager {
    void reload();

    void set(String path, Object value);

    YamlConfiguration getYml();

    void save();

    List<String> getList(String path);

    boolean getBoolean(String path);

    int getInt(String path);

    double getDouble(String path);

    String getString(String path);

    boolean isFirstTime(String path);

    String getName();
}

package org.twightlight.hlootchest.api.interfaces;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public interface TConfigManager {
    void reload();

    void set(String path, Object value);

    void setNotSave(String path, Object value);

    YamlConfiguration getYml();

    void save();

    List<String> getList(String path);

    boolean getBoolean(String path);

    int getInt(String path);

    double getDouble(String path);

    String getString(String path);

    boolean getBoolean(String path, Boolean fallback);

    int getInt(String path, Integer fallback);

    double getDouble(String path, Double fallback);

    String getString(String path, String fallback);

    boolean isFirstTime(String path);

    String getName();
}

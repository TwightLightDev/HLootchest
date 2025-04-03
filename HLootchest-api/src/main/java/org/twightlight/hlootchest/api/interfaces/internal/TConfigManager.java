package org.twightlight.hlootchest.api.interfaces.internal;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public interface TConfigManager {
    /**
     * Reloads the configuration, discarding any unsaved changes.
     */
    void reload();

    /**
     * Sets a value at the specified path in the configuration.
     *
     * @param path The configuration path.
     * @param value The value to set.
     */
    void set(String path, Object value);

    /**
     * Sets a value at the specified path without saving it to the file.
     *
     * @param path The configuration path.
     * @param value The value to set.
     */
    void setNotSave(String path, Object value);

    /**
     * Retrieves the underlying {@link YamlConfiguration} instance.
     *
     * @return The {@link YamlConfiguration} representing the configuration data.
     */
    YamlConfiguration getYml();

    /**
     * Saves the current state of the configuration to the file.
     */
    void save();

    /**
     * Retrieves a list of strings from the configuration.
     *
     * @param path The configuration path.
     * @return A {@link List} of {@link String}, or {@code null} if the path does not exist.
     */
    List<String> getList(String path);

    /**
     * Retrieves a boolean value from the configuration.
     *
     * @param path The configuration path.
     * @return The boolean value at the specified path.
     */
    boolean getBoolean(String path);

    /**
     * Retrieves an integer value from the configuration.
     *
     * @param path The configuration path.
     * @return The integer value at the specified path.
     */
    int getInt(String path);

    /**
     * Retrieves a double value from the configuration.
     *
     * @param path The configuration path.
     * @return The double value at the specified path.
     */
    double getDouble(String path);

    /**
     * Retrieves a string value from the configuration.
     *
     * @param path The configuration path.
     * @return The string value at the specified path, or {@code null} if not found.
     */
    String getString(String path);

    /**
     * Retrieves a boolean value from the configuration, returning a fallback value if the path does not exist.
     *
     * @param path The configuration path.
     * @param fallback The fallback value to return if the path is not found.
     * @return The boolean value at the specified path, or the fallback value if not found.
     */
    boolean getBoolean(String path, Boolean fallback);

    /**
     * Retrieves an integer value from the configuration, returning a fallback value if the path does not exist.
     *
     * @param path The configuration path.
     * @param fallback The fallback value to return if the path is not found.
     * @return The integer value at the specified path, or the fallback value if not found.
     */
    int getInt(String path, Integer fallback);

    /**
     * Retrieves a double value from the configuration, returning a fallback value if the path does not exist.
     *
     * @param path The configuration path.
     * @param fallback The fallback value to return if the path is not found.
     * @return The double value at the specified path, or the fallback value if not found.
     */
    double getDouble(String path, Double fallback);

    /**
     * Retrieves a string value from the configuration, returning a fallback value if the path does not exist.
     *
     * @param path The configuration path.
     * @param fallback The fallback value to return if the path is not found.
     * @return The string value at the specified path, or the fallback value if not found.
     */
    String getString(String path, String fallback);

    /**
     * Checks whether the specified path is being accessed for the first time.
     *
     * @param path The configuration path.
     * @return {@code true} if the path is being accessed for the first time, otherwise {@code false}.
     */
    boolean isFirstTime(String path);

    /**
     * Retrieves the name of the configuration.
     *
     * @return The name of the configuration as a {@link String}.
     */
    String getName();
}

package org.twightlight.hlootchest.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

    public class ConfigManager implements TConfigManager {
        private YamlConfiguration yml;

        private File config;

        private String name;

        private boolean firstTime = false;

        public ConfigManager(Plugin plugin, String name, String dir) {
            File d = new File(dir);
            if (!d.exists() &&
                    !d.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + d.getPath());
                return;
            }
            this.config = new File(dir, name + ".yml");
            if (!this.config.exists()) {
                this.firstTime = true;
                plugin.getLogger().log(Level.INFO, "Creating " + this.config.getPath());
                try {
                    if (!this.config.createNewFile()) {
                        plugin.getLogger().log(Level.SEVERE, "Could not create " + this.config.getPath());
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.yml = YamlConfiguration.loadConfiguration(this.config);
            this.yml.options().copyDefaults(true);
            this.name = name;
        }

        public void reload() {
            if (!this.config.exists()) {
                System.err.println("Config file does not exist: " + this.config.getPath());
                return;
            }
            this.yml = YamlConfiguration.loadConfiguration(this.config);
        }

        public void set(String path, Object value) {
            this.yml.set(path, value);
            save();
        }

        public void setNotSave(String path, Object value) {
            this.yml.set(path, value);
        }

        public YamlConfiguration getYml() {
            return this.yml;
        }

        public void save() {
            try {
                this.yml.save(this.config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public List<String> getList(String path) {
            return this.yml.getStringList(path).stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
        }

        public boolean getBoolean(String path) {
            return this.yml.getBoolean(path);
        }

        public int getInt(String path) {
            return this.yml.getInt(path);
        }

        public double getDouble(String path) {
            return this.yml.getDouble(path);
        }

        public String getString(String path) {
            return this.yml.getString(path);
        }

        public boolean getBoolean(String path, Boolean fallBack) {
            return this.yml.getBoolean(path, fallBack);
        }

        public int getInt(String path, Integer fallBack) {
            return this.yml.getInt(path, fallBack);
        }

        public double getDouble(String path, Double fallBack) {
            return this.yml.getDouble(path, fallBack);
        }

        public String getString(String path, String fallBack) {
            return this.yml.getString(path, fallBack);
        }

        @Override
        public boolean isFirstTime(String path) {
            return this.firstTime;
        }

        public String getName() {
            return this.name;
        }
    }


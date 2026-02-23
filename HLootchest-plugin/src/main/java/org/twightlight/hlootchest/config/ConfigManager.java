package org.twightlight.hlootchest.config;

import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.config.configs.MainConfig;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public static TYamlWrapper mainConfig;
    public static TYamlWrapper templateConfig;
    public static Map<String, TYamlWrapper> boxesConfigMap = new HashMap<>();
    public static TYamlWrapper messagesConfig;
    private static String path = HLootChest.getInstance().getDataFolder().getPath();
    public static TYamlWrapper registration;

    public static void init() {
        Utility.info("&eLoading config.yml...");
        mainConfig = new MainConfig(HLootChest.getInstance(), "config", path);

        Utility.info("&eLoading templates...");
        File file = new File((HLootChest.getInstance().getDataFolder().getPath()+ "/templates"), "example_template.yml");
        if (!file.exists()) {
            HLootChest.getInstance().saveResource("templates/example_template.yml", false);
        }
        templateConfig = new YamlWrapper(HLootChest.getInstance(), mainConfig.getString("template"), HLootChest.getInstance().getDataFolder().getPath()+ "/templates");

        Utility.info("&eLoading messages.yml...");
        File file4 = new File(HLootChest.getInstance().getDataFolder().getPath(), "messages.yml");
        if (!file4.exists()) {
            HLootChest.getInstance().saveResource("messages.yml", false);
        }
        messagesConfig = new YamlWrapper(HLootChest.getInstance(), "messages", path);

        Utility.info("&eLoading registrations.yml...");
        File file5 = new File(HLootChest.getInstance().getDataFolder().getPath(), "registrations.yml");
        if (!file5.exists()) {
            HLootChest.getInstance().saveResource("registrations.yml", false);
        }
        registration = new YamlWrapper(HLootChest.getInstance(), "registrations", path);

        File file6 = new File((HLootChest.getInstance().getDataFolder().getPath()+ "/lootchests"), "regular.yml");
        if (!file6.exists()) {
            HLootChest.getInstance().saveResource("lootchests/" + "regular.yml", false);
        }
        File file7 = new File((HLootChest.getInstance().getDataFolder().getPath()+ "/lootchests"), "mystic.yml");
        if (!file7.exists()) {
            HLootChest.getInstance().saveResource("lootchests/" + "mystic.yml", false);
        }
        File file8 = new File((HLootChest.getInstance().getDataFolder().getPath()+ "/lootchests"), "spooky.yml");
        if (!file8.exists()) {
            HLootChest.getInstance().saveResource("lootchests/" + "spooky.yml", false);
        }
        File file9 = new File((HLootChest.getInstance().getDataFolder().getPath()+ "/lootchests"), "aeternus.yml");
        if (!file9.exists()) {
            HLootChest.getInstance().saveResource("lootchests/" + "aeternus.yml", false);
        }
    }
}

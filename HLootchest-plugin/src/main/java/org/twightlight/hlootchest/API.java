package org.twightlight.hlootchest;

import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;

public class API implements HLootchest {
    private final HLootchest.ConfigUtil configUtil = new ConfigUtil();

    private static class ConfigUtil implements HLootchest.ConfigUtil {

        public TConfigManager getTemplateConfig() {
            return org.twightlight.hlootchest.HLootchest.templateConfig;
        }
        public TConfigManager getMainConfig() {
            return org.twightlight.hlootchest.HLootchest.mainConfig;
        }
        public TConfigManager getBoxesConfig() {
            return org.twightlight.hlootchest.HLootchest.boxesConfig;
        }
        public TConfigManager getMessageConfig() { return org.twightlight.hlootchest.HLootchest.messagesConfig; }
    }

    public HLootchest.ConfigUtil getConfigUtil() {
        return configUtil;
    }

}

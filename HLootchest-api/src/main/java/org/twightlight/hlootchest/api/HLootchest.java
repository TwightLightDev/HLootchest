package org.twightlight.hlootchest.api;

import org.twightlight.hlootchest.api.objects.TConfigManager;

public interface HLootchest {
    ConfigUtil getConfigUtil();



    interface ConfigUtil {
        TConfigManager getTemplateConfig();
        TConfigManager getMainConfig();
        TConfigManager getBoxesConfig();
    }
}

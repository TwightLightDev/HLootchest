package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.List;

public class IconSettings extends BaseMenu {

    private static final List<String> SLOTS = Arrays.asList("HEAD", "CHESTPLATE", "LEGGINGS", "BOOTS", "MAIN_HAND", "OFF_HAND");
    private final Executable backAction;

    public IconSettings(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Editing icon...", () -> new IconSettings(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        validatedChatItem(11,
                XMaterial.valueOf(templateFile.getString(fullPath(".material"), "BEDROCK")),
                "&bMaterial", ".material",
                input -> XMaterial.matchXMaterial(input).isPresent(),
                "&cInvalid Material! Cancel the action!");

        numericChatItem(12, XMaterial.YELLOW_WOOL, "&bData", ".data");

        toggleItem(13, XMaterial.GLOWSTONE_DUST, "&bGlowing", ".glowing", false);

        cycleItem(14, XMaterial.ARMOR_STAND, "&bSlot", ".slot", SLOTS, "null");

        if (XMaterial.PLAYER_HEAD == XMaterial.valueOf(templateFile.getString(fullPath(".material"), "BEDROCK"))) {
            stringChatItem(15, XMaterial.NAME_TAG, "&bHead value", ".head_value");
        }
    }
}

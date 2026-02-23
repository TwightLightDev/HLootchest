package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.BaseMenu;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.List;

public class Name extends BaseMenu {

    private static final List<String> NAME_MODES = Arrays.asList("always", "hover");
    private final boolean isChild;
    private final Executable backAction;

    public Name(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.isChild = isChild;
        this.backAction = backAction;
        open(27, "&7Editing button...", () -> new Name(p, templateFile, name, path, session, isChild, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        toggleItem(11, XMaterial.NAME_TAG, "&bEnable", ".enable", false);
        cycleItem(12, XMaterial.ARMOR_STAND, "&bVisible mode", ".visible-mode", NAME_MODES, "always");
        toggleItem(13, XMaterial.NAME_TAG, "&bDynamic", ".dynamic", false);

        boolean dynamic = templateFile.getBoolean(fullPath(".dynamic"), false);
        if (dynamic) {
            editableListItem(14, XMaterial.FEATHER, "&bDisplay Name", ".display-name");
            numericChatItem(15, XMaterial.CLOCK, "&bRefresh interval", ".refresh-interval");
        } else {
            stringChatItem(14, XMaterial.FEATHER, "&bDisplay Name", ".display-name");
        }
    }
}

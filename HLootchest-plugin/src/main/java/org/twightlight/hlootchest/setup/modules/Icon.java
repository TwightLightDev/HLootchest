package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.elements.IconsMenu;
import org.twightlight.libs.xseries.XMaterial;

public class Icon extends BaseMenu {

    private final boolean isChild;
    private final Executable backAction;

    public Icon(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.isChild = isChild;
        this.backAction = backAction;
        open(27, "&7Editing icon...", () -> new Icon(p, templateFile, name, path, session, isChild, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        toggleItem(11, XMaterial.NAME_TAG, "&bDynamic", ".dynamic", false);

        boolean dynamic = templateFile.getBoolean(fullPath(".dynamic"), false);
        if (dynamic) {
            submenuItem(12, XMaterial.PLAYER_HEAD, "&bIcon List",
                    e -> new IconsMenu(p, templateFile, name, path + ".dynamic-icons", session, isChild, backAction));
            numericChatItem(13, XMaterial.CLOCK, "&bRefresh interval", ".refresh-interval");
        } else {
            submenuItem(12, XMaterial.valueOf(templateFile.getString(fullPath(".material"), "BEDROCK")), "&bIcon Settings",
                    e -> new IconSettings(p, templateFile, name, path, session,
                            ev -> new Icon(p, templateFile, name, path, session, isChild, backAction)));
        }
    }
}

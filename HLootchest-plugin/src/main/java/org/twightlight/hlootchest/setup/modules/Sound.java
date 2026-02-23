package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.BaseMenu;
import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;

public class Sound extends BaseMenu {

    private final Executable backAction;

    public Sound(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Settings", () -> new Sound(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        validatedChatItem(11, XMaterial.CHEST, "&bSound Type", ".sound",
                input -> XSound.matchXSound(input).isPresent(), "&cInvalid Sound! Action canceled.");

        numericChatItem(12, XMaterial.CHEST, "&bYaw", ".yaw");
        numericChatItem(13, XMaterial.CHEST, "&bPitch", ".pitch");
    }
}

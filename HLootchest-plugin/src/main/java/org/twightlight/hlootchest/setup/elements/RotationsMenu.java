package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.modules.Rotation;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class RotationsMenu extends BaseMenu {

    private final Executable backAction;

    public RotationsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Rotations", () -> new RotationsMenu(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Rotation", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis rotation name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new rotation: &e" + input);
                    new Rotation(p, templateFile, name, path + "." + input, session, backAction);
                }));

        int i = 0;
        for (String rot : getKeys("")) {
            item(i, XMaterial.ARMOR_STAND, "&eName: " + ChatColor.LIGHT_PURPLE + rot,
                    Arrays.asList(
                            "&aPosition: " + templateFile.getYml().getString(fullPath("." + rot + ".position"), "null"),
                            "&aValue: " + templateFile.getYml().getString(fullPath("." + rot + ".value"), "null"),
                            "", "&eLeft-click to edit!", "&eRight-click to remove!"),
                    e -> {
                        if (e.isLeftClick()) new Rotation(p, templateFile, name, path + "." + rot, session, backAction);
                        else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this rotation!");
                            templateFile.getYml().set(fullPath("." + rot), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.BaseMenu;
import org.twightlight.hlootchest.setup.ChatPrompt;
import org.twightlight.hlootchest.setup.modules.Requirement;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class RequirementsMenu extends BaseMenu {

    private final Executable backAction;

    public RequirementsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Requirements", () -> new RequirementsMenu(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Requirement", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis requirement name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new requirement: &e" + input);
                    new Requirement(p, templateFile, name, path + "." + input, session, backAction);
                }));

        int i = 0;
        for (String req : getKeys("")) {
            item(i, XMaterial.RED_WOOL, "&eName: " + ChatColor.AQUA + req,
                    Arrays.asList("&aType: " + templateFile.getYml().getString(fullPath("." + req + ".type"), "null"),
                            "", "&eLeft-click to edit!", "&eRight-click to remove!"),
                    e -> {
                        if (e.isLeftClick()) new Requirement(p, templateFile, name, path + "." + req, session, backAction);
                        else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this requirement!");
                            templateFile.getYml().set(fullPath("." + req), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

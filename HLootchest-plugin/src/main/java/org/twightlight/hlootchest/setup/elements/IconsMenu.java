package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.modules.IconSettings;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class IconsMenu extends BaseMenu {

    private final boolean isChild;
    private final Executable backAction;

    public IconsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.isChild = isChild;
        this.backAction = backAction;
        open(27, "&7Icons", () -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, backAction);

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Icon", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis icon name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new icon: &e" + input);
                    new IconSettings(p, templateFile, name, path + "." + input, session,
                            ev -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
                }));

        int i = 0;
        for (String icon : getKeys("")) {
            item(i, XMaterial.valueOf(templateFile.getString(fullPath("." + icon + ".material"), "BEDROCK")),
                    templateFile.getString(fullPath("." + icon + ".head_value"), ""),
                    templateFile.getInt(fullPath("." + icon + ".data"), 0),
                    "&eName: " + ChatColor.AQUA + icon,
                    Arrays.asList("", "&eLeft-click to edit!", "&eRight-click to remove!"), false,
                    e -> {
                        if (e.isLeftClick()) new IconSettings(p, templateFile, name + "." + icon, path, session,
                                ev -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
                        else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this icon!");
                            templateFile.getYml().set(fullPath("." + icon), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

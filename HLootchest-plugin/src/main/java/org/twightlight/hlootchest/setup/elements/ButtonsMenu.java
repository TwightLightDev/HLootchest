package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.main.TemplateMenu;
import org.twightlight.hlootchest.setup.modules.Button;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class ButtonsMenu extends BaseMenu {

    public ButtonsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session) {
        super(p, templateFile, name, path, session);
        open(27, "&7Buttons list", () -> new ButtonsMenu(p, templateFile, name, path, session));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new TemplateMenu(p, templateFile, name, session));

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Button", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis button name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new button: &e" + input);
                    new Button(p, templateFile, name, path + "." + input, session, false);
                }));

        int i = 0;
        for (String button : getKeys("")) {
            item(i, XMaterial.STONE_BUTTON, "&eName: " + ChatColor.AQUA + button,
                    Arrays.asList("", "&eLeft-click to edit!", "&eRight-click to remove!"),
                    e -> {
                        if (e.isLeftClick()) new Button(p, templateFile, name, path + "." + button, session, false);
                        else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this button!");
                            templateFile.getYml().set(fullPath("." + button), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

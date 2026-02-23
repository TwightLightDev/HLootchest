package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.modules.Button;
import org.twightlight.hlootchest.setup.modules.Reward;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class ChildrenMenu extends BaseMenu {

    private final boolean isReward;

    public ChildrenMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isReward) {
        super(p, templateFile, name, path, session);
        this.isReward = isReward;
        open(27, "&7Children list", () -> new ChildrenMenu(p, templateFile, name, path, session, isReward));
    }

    @Override
    protected void populate() {
        backButton(18, e -> {
            String prevPath = Utility.getPrevPath(path);
            if (isReward) new Reward(p, templateFile, name, prevPath, session, false);
            else new Button(p, templateFile, name, prevPath, session, false);
        });

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Child", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis child name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new child: &e" + input);
                    if (isReward) new Reward(p, templateFile, name, path + "." + input, session, true);
                    else new Button(p, templateFile, name, path + "." + input, session, true);
                }));

        int i = 0;
        for (String child : getKeys("")) {
            item(i, XMaterial.STONE_BUTTON, "&eName: " + ChatColor.AQUA + child,
                    Arrays.asList("", "&eLeft-click to edit!", "&eRight-click to remove!"),
                    e -> {
                        if (e.isLeftClick()) {
                            if (isReward) new Reward(p, templateFile, name, path + "." + child, session, true);
                            else new Button(p, templateFile, name, path + "." + child, session, true);
                        } else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this child!");
                            templateFile.getYml().set(fullPath("." + child), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

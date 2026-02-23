package org.twightlight.hlootchest.setup.elements;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.BaseMenu;
import org.twightlight.hlootchest.setup.ChatPrompt;
import org.twightlight.hlootchest.setup.LootChestSetupMenu;
import org.twightlight.hlootchest.setup.modules.Reward;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;

public class RewardsMenu extends BaseMenu {

    public RewardsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session) {
        super(p, templateFile, name, path, session);
        open(27, "&7Rewards list", () -> new RewardsMenu(p, templateFile, name, path, session));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new LootChestSetupMenu(p, templateFile, name, session));

        item(26, XMaterial.SLIME_BALL, ChatColor.GREEN + "Add New Reward", Collections.emptyList(),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    if (getKeys("").contains(input)) {
                        msg("&cThis reward name already exists! Cancel the action!");
                        buildAndOpen();
                        return;
                    }
                    msg("&aYou successfully created new reward: &e" + input);
                    new Reward(p, templateFile, name, path + "." + input, session, false);
                }));

        int i = 0;
        for (String reward : getKeys("")) {
            item(i, XMaterial.EMERALD, "&eName: " + ChatColor.AQUA + reward,
                    Arrays.asList("", "&eLeft-click to edit!", "&eRight-click to remove!"),
                    e -> {
                        if (e.isLeftClick()) new Reward(p, templateFile, name, path + "." + reward, session, false);
                        else if (e.isRightClick()) {
                            msg("&aYou have successfully removed this reward!");
                            templateFile.getYml().set(fullPath("." + reward), null);
                            buildAndOpen();
                        }
                    });
            i++;
        }
    }
}

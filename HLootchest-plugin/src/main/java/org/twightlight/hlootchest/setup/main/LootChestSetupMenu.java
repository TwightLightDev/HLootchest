package org.twightlight.hlootchest.setup.main;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.browse.LootChestBrowseMenu;
import org.twightlight.hlootchest.setup.elements.RewardsMenu;
import org.twightlight.hlootchest.setup.modules.IconSettings;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;

public class LootChestSetupMenu extends BaseMenu {

    public LootChestSetupMenu(Player p, TYamlWrapper templateFile, String name, SetupSession session) {
        super(p, templateFile, name, "", session);
        open(27, "&7Editing " + name + "...", () -> new LootChestSetupMenu(p, templateFile, name, session));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new LootChestBrowseMenu(p));

        item(11, XMaterial.valueOf(templateFile.getString(name + ".icon.material", "BEDROCK")),
                templateFile.getString(name + ".icon.head_value", ""),
                templateFile.getInt(name + ".icon.data", 0),
                "&bIcon", Arrays.asList("&aClick to browse!"), false,
                e -> new IconSettings(p, templateFile, name, ".icon", session,
                        ev -> new LootChestSetupMenu(p, templateFile, name, session)));

        numericChatItem(12, XMaterial.CHEST, "&bRewards amount", ".reward-amount");

        submenuItem(13, XMaterial.EMERALD, "&bRewards",
                e -> new RewardsMenu(p, templateFile, name, ".rewards-list", session));
    }
}

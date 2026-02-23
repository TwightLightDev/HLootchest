package org.twightlight.hlootchest.setup.browse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.main.LootChestSetupMenu;
import org.twightlight.hlootchest.utils.DyeColor;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Collections;
import java.util.Set;

public class LootChestBrowseMenu extends BaseMenu {

    public LootChestBrowseMenu(Player p) {
        super(p, null, "", "", resolveSession(p));
        if (session == null) return;
        open(54, "&7LootChest Setup", () -> new LootChestBrowseMenu(p));
    }

    private static SetupSession resolveSession(Player p) {
        TSession s = HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        return s instanceof SetupSession ? (SetupSession) s : null;
    }

    @Override
    protected void populate() {
        Set<String> lcList = HLootChest.getAPI().getNMS().getRegistrationData().keySet();
        int i = 0;
        for (String lc : lcList) {
            item(i, XMaterial.CHEST, ChatColor.GREEN + lc, Collections.emptyList(),
                    e -> new LootChestSetupMenu(p, HLootChest.getAPI().getConfigUtil().getBoxesConfig(lc), lc, session));
            i++;
        }

        item(44, XMaterial.RED_WOOL, "", DyeColor.RED.getColorData(),
                ChatColor.RED + "Exit", Collections.emptyList(), false,
                e -> { session.close(); p.closeInventory(); msg("&aYou successfully exited this setup!"); });

        item(53, XMaterial.LIME_WOOL, "", DyeColor.LIME.getColorData(),
                ChatColor.GREEN + "Save", Collections.emptyList(), false,
                e -> {
                    for (String type : HLootChest.getAPI().getConfigUtil().getBoxesConfigs().keySet()) {
                        TYamlWrapper tf = HLootChest.getAPI().getConfigUtil().getBoxesConfig(type);
                        tf.save();
                        tf.reload();
                        msg("&aYou successfully saved " + type + ".yml!");
                    }
                    p.closeInventory();
                    session.close();
                });
    }
}

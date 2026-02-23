package org.twightlight.hlootchest.setup.browse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.main.TemplateMenu;
import org.twightlight.hlootchest.utils.DyeColor;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Collections;
import java.util.Set;

public class TemplateBrowseMenu extends BaseMenu {

    public TemplateBrowseMenu(Player p, TYamlWrapper templateFile) {
        super(p, templateFile, "", "", resolveSession(p));
        if (session == null) return;
        open(54, "&7Template Setup", () -> new TemplateBrowseMenu(p, templateFile));
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
                    e -> new TemplateMenu(p, templateFile, lc, session));
            i++;
        }

        item(44, XMaterial.RED_WOOL, "", DyeColor.RED.getColorData(),
                ChatColor.RED + "Exit", Collections.emptyList(), false,
                e -> { session.close(); p.closeInventory(); msg("&aYou successfully exited this setup!"); });

        item(53, XMaterial.LIME_WOOL, "", DyeColor.LIME.getColorData(),
                ChatColor.GREEN + "Save", Collections.emptyList(), false,
                e -> {
                    templateFile.save();
                    templateFile.reload();
                    p.closeInventory();
                    session.close();
                    msg("&aYou successfully saved this template!");
                });
    }
}

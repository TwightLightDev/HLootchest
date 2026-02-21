package org.twightlight.hlootchest.setup;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.utils.DyeColor;

import java.util.Collections;
import java.util.Set;

public class TemplateBrowseMenu {

    public TemplateBrowseMenu(Player p, TYamlWrapper templateFile) {
        if (HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
            return;
        }
        TSession session1 = HLootChest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        SetupSession session;
        if (session1 instanceof SetupSession) {
            session = ((SetupSession) session1);
        } else {
            return;
        }
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        Set<String> lcList = HLootChest.getAPI().getNMS().getRegistrationData().keySet();
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&7Template Setup"));
        session.setInvConstructor((MenuHandler<TemplateBrowseMenu>) () -> new TemplateBrowseMenu(p, templateFile));
        int i = 0;
        for (String lc : lcList) {
            MenuManager.setItem(p,
                    inv,
                    HLootChest.getNms().createItem(XMaterial.valueOf("CHEST").parseMaterial(), "", 0, ChatColor.GREEN + lc, Collections.emptyList(), false),
                    i,
                    (e) -> {
                new TemplateMenu(p, templateFile, lc, session);
            });
            i ++;
        }

        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(
                        XMaterial.RED_WOOL.parseMaterial(),
                        "",
                        DyeColor.RED.getColorData(),
                        ChatColor.RED + "Exit",
                        Collections.emptyList(),
                        false),
                44,
                (e) -> {
                    session.close();
                    p.closeInventory();
                    p.sendMessage(ChatColor.GREEN + "You successfully exited this setup!");
                });
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(
                        XMaterial.LIME_WOOL.parseMaterial(),
                        "",
                        DyeColor.LIME.getColorData(),
                        ChatColor.GREEN + "Save",
                        Collections.emptyList(),
                        false),
                53,
                (e) -> {
                    templateFile.save();
                    templateFile.reload();
                    p.closeInventory();
                    session.close();
                    p.sendMessage(ChatColor.GREEN + "You successfully saved this template!");
                });
        p.openInventory(inv);
    }

}

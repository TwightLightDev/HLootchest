package org.twightlight.hlootchest.setup.menus;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;

import java.util.ArrayList;
import java.util.Set;

public class MainMenu {

    public MainMenu(Player p, TConfigManager templateFile) {
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
            return;
        }
        TSessions session1 = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        SetupSessions session;
        if (session1 instanceof SetupSessions) {
            session = ((SetupSessions) session1);
        } else {
            return;
        }
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        Set<String> lcList = HLootchest.getAPI().getNMS().getRegistrationData().keySet();
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&7Template Setup"));
        session.setInvConstructor((MenuHandler<MainMenu>) () -> new MainMenu(p, templateFile));
        int i = 0;
        for (String lc : lcList) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.valueOf("CHEST").parseMaterial(), "", 0, ChatColor.GREEN + lc, new ArrayList<>(), false),
                    i,
                    (e) -> {
                new TemplateMenu(p, templateFile, lc, session);
            });
            i ++;
        }
        p.openInventory(inv);
    }

}

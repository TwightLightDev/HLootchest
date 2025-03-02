package org.twightlight.hlootchest.setup;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;

import java.util.Collections;
import java.util.Set;

public class LCSMainMenu {

    public LCSMainMenu(Player p, TConfigManager templateFile) {
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) == null) {
            return;
        }
        TSession session1 = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p);
        SetupSession session;
        if (session1 instanceof SetupSession) {
            session = ((SetupSession) session1);
        } else {
            return;
        }
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        Set<String> lcList = HLootchest.getAPI().getNMS().getRegistrationData().keySet();
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&7LootChest Setup"));
        session.setInvConstructor((MenuHandler<LCSMainMenu>) () -> new LCSMainMenu(p, templateFile));
        int i = 0;
        for (String lc : lcList) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.valueOf("CHEST").parseMaterial(), "", 0, ChatColor.GREEN + lc, Collections.emptyList(), false),
                    i,
                    (e) -> {
                new LCMenu(p, templateFile, lc, session);
            });
            i ++;
        }

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.RED_WOOL.parseMaterial(),
                        "",
                        0,
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
                HLootchest.getNms().createItem(
                        XMaterial.GREEN_WOOL.parseMaterial(),
                        "",
                        0,
                        ChatColor.GREEN + "Save",
                        Collections.emptyList(),
                        false),
                53,
                (e) -> {
                    templateFile.save();
                    templateFile.reload();
                    p.closeInventory();
                    session.close();
                    p.sendMessage(ChatColor.GREEN + "You successfully saved lootchests.yml!");
                });
        p.openInventory(inv);
    }

}

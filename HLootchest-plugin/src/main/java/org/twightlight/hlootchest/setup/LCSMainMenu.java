package org.twightlight.hlootchest.setup;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ProtocolVersion;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.utils.DyeColor;

import java.util.Collections;
import java.util.Set;

public class LCSMainMenu {

    public LCSMainMenu(Player p) {
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
        session.setInvConstructor((MenuHandler<LCSMainMenu>) () -> new LCSMainMenu(p));
        int i = 0;
        for (String lc : lcList) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.valueOf("CHEST").parseMaterial(), "", 0, ChatColor.GREEN + lc, Collections.emptyList(), false),
                    i,
                    (e) -> {
                new LCMenu(p, HLootchest.getAPI().getConfigUtil().getBoxesConfig(lc), lc, session);
            });
            i ++;
        }

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
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
                HLootchest.getNms().createItem(
                        XMaterial.LIME_WOOL.parseMaterial(),
                        "",
                        DyeColor.LIME.getColorData(),
                        ChatColor.GREEN + "Save",
                        Collections.emptyList(),
                        false),
                53,
                (e) -> {

                    Set<String> types = HLootchest.getAPI().getConfigUtil().getBoxesConfigs().keySet();
                    for (String type : types) {
                        TConfigManager templateFile = HLootchest.getAPI().getConfigUtil().getBoxesConfig(type);
                        templateFile.save();
                        templateFile.reload();
                        p.sendMessage(ChatColor.GREEN + "You successfully saved " + type + ".yml!");
                    }
                    p.closeInventory();
                    session.close();
                });
        p.openInventory(inv);
    }

}

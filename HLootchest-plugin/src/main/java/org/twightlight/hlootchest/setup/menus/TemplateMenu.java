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
import org.twightlight.hlootchest.setup.menus.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.menus.modules.Settings;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateMenu {

    public TemplateMenu(Player p, TConfigManager templateFile, String name, SetupSessions session) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing " + name + "...");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<TemplateMenu>) () -> new TemplateMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                (e) -> new MainMenu(p, templateFile));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.COMPARATOR.parseMaterial(),
                        "",
                        0,
                        ChatColor.YELLOW + "Settings",
                        Arrays.asList(new String[] {"&aClick to browse!"}),
                        false),
                11,
                (e) -> new Settings(p, templateFile, name, ".settings", session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.STONE_BUTTON.parseMaterial(),
                        "",
                        0,
                        ChatColor.YELLOW + "Buttons",
                        Arrays.asList(new String[] {"&aClick to browse!"}),
                        false),
                13,
                (e) -> new ButtonsMenu(p, templateFile, name, ".buttons", session));
        List<String> locs = new ArrayList<>();
        if (templateFile.getYml().contains(name + ".rewards-location")) {
            locs = templateFile.getList(name + ".rewards-location");
        }
        locs = locs.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', "&f" + "- " + line))
                .collect(Collectors.toList());
        locs.add("");
        locs.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add your current location."));
        locs.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last location."));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.ARMOR_STAND.parseMaterial(),
                        "",
                        0,
                        ChatColor.YELLOW + "Rewards Location",
                        locs,
                        false),
                15,
                (e) -> {
                    List<String> locs1 = new ArrayList<>();
                    if (templateFile.getYml().contains(name + ".rewards-location")) {
                        locs1 = templateFile.getList(name + ".rewards-location");
                    }
                    if (e.isLeftClick()) {
                        locs1.add(Utility.locationToString(p.getLocation()));
                        templateFile.setNotSave(name + ".rewards-location", locs1);
                        new TemplateMenu(p, templateFile, name, session);
                    } else if (e.isRightClick()) {
                        locs1.remove(locs1.size()-1);
                        templateFile.setNotSave(name + ".rewards-location", locs1);
                        new TemplateMenu(p, templateFile, name, session);
                    }
                });

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.RED_WOOL.parseMaterial(),
                        "",
                        0,
                        ChatColor.RED + "Exit",
                        new ArrayList<>(),
                        false),
                17,
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
                        new ArrayList<>(),
                        false),
                26,
                (e) -> {
                templateFile.save();
                p.closeInventory();
                p.sendMessage(ChatColor.GREEN + "You successfully saved this template!");
                });


        p.openInventory(inv);
    }
}

package org.twightlight.hlootchest.setup;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.modules.Settings;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateMenu {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final SetupSession session;

    public TemplateMenu(Player p, TConfigManager templateFile, String name, SetupSession session) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.session = session;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing " + name + "...");


        session.setInvConstructor((MenuHandler<TemplateMenu>) () -> new TemplateMenu(p, templateFile, name, session));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                18,
                (e) -> new TSMainMenu(p, templateFile));
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
        List<String> locs = Collections.emptyList();
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
                    List<String> locs1 = Collections.emptyList();
                    if (templateFile.getYml().contains(name + ".rewards-location")) {
                        locs1 = templateFile.getList(name + ".rewards-location");
                    }
                    if (e.isLeftClick()) {
                        locs1.add(Utility.locationToString(p.getLocation()));
                        templateFile.setNotSave(name + ".rewards-location", locs1);
                        setItems(inv);
                    } else if (e.isRightClick()) {
                        locs1.remove(locs1.size()-1);
                        templateFile.setNotSave(name + ".rewards-location", locs1);
                        setItems(inv);
                    }
                });


        p.openInventory(inv);
    }
}

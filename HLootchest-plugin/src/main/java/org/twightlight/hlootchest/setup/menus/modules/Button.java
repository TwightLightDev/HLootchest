package org.twightlight.hlootchest.setup.menus.modules;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.TemplateMenu;
import org.twightlight.hlootchest.setup.menus.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.menus.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class Button {
    public Button(Player p, TConfigManager templateFile, String name, String path, SetupSessions session) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GRAY + "Editing button...");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<Button>) () -> new Button(p, templateFile, name, path, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                45,
                (e) -> new TemplateMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bLocation",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".location", "null"),
                                "", "&eClick to set to your current location!"}),
                        false),
                11,
                (e) -> {
                    templateFile.setNotSave(name + path + ".location", Utility.locationToString(p.getLocation()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                    new Button(p, templateFile, name, path, session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bClick sound",
                        Arrays.asList(new String[] {"&eClick to browse!"}),
                        false),
                12,
                (e) -> {
                    new Sound(p, templateFile, name, path + ".click-sound", session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bHover sound",
                        Arrays.asList(new String[] {"&eClick to browse!"}),
                        false),
                13,
                (e) -> {
                    new Sound(p, templateFile, name, path + ".hover-sound", session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
                        "&bRotations",
                        Arrays.asList(new String[] {"&eClick to browse!"}),
                        false),
                14,
                (e) -> {
                    new RotationsMenu(p, templateFile, name, path+".rotations", session, (ev) -> new Button(p, templateFile, name, path, session));
                });
        p.openInventory(inv);
    }
}

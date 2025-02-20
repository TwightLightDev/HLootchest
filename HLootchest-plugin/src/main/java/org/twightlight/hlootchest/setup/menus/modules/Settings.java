package org.twightlight.hlootchest.setup.menus.modules;

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
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.TemplateMenu;
import org.twightlight.hlootchest.setup.menus.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class Settings {
    public Settings(Player p, TConfigManager templateFile, String name, String path, SetupSessions session) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Settings");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<Settings>) () -> new Settings(p, templateFile, name, path, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                (e) -> new TemplateMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bPlayer's location",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".player-location", "null"),
                                "", "&eClick to set to your current location!"}),
                        false),
                11,
                (e) -> {
                    templateFile.setNotSave(name + path + ".player-location", Utility.locationToString(p.getLocation()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                    new Settings(p, templateFile, name, path, session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bLocation",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".location", "null"),
                                "", "&eClick to set to your current location!"}),
                        false),
                12,
                (e) -> {
                    templateFile.setNotSave(name + path + ".location", Utility.locationToString(p.getLocation()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                    new Settings(p, templateFile, name, path, session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.LEVER.parseMaterial(), "", 0,
                        "&bClick-to-open",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".click-to-open", false)),
                                "", "&eClick to change!"}),
                        false),
                13,
                (e) -> {
                    templateFile.setNotSave(name + path + ".click-to-open", !templateFile.getBoolean(name + path + ".click-to-open", false));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".click-to-open")));
                    new Settings(p, templateFile, name, path, session);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
                        "&bRotations",
                        Arrays.asList(new String[] {"&eClick to browse!"}),
                        false),
                14,
                (e) -> {
                    new RotationsMenu(p, templateFile, name, path+".rotations", session, (ev) -> new Settings(p, templateFile, name, path, session));
                });
        p.openInventory(inv);
    }
}

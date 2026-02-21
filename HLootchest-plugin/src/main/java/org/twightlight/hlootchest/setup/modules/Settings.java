package org.twightlight.hlootchest.setup.modules;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.setup.TemplateMenu;
import org.twightlight.hlootchest.setup.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;

public class Settings {
    private final Player p;
    private final TYamlWrapper templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;

    public Settings(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Settings");

        session.setInvConstructor((MenuHandler<Settings>) () -> new Settings(p, templateFile, name, path, session));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                18,
                (e) -> new TemplateMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bPlayer's location",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".player-location", "null"),
                                "", "&eClick to set to your current location!"}),
                        false),
                11,
                (e) -> {
                    templateFile.setNotSave(name + path + ".player-location", Utility.locationToString(p.getLocation()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                    setItems(inv);
                });
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bLocation",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".location", "null"),
                                "", "&eClick to set to your current location!"}),
                        false),
                12,
                (e) -> {
                    templateFile.setNotSave(name + path + ".location", Utility.locationToString(p.getLocation()));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                    setItems(inv);
                });
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.LEVER.parseMaterial(), "", 0,
                        "&bClick-to-open",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".click-to-open", false)),
                                "", "&eClick to change!"}),
                        false),
                13,
                (e) -> {
                    templateFile.setNotSave(name + path + ".click-to-open", !templateFile.getBoolean(name + path + ".click-to-open", false));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".click-to-open")));
                    setItems(inv);
                });
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
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

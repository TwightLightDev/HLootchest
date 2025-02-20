package org.twightlight.hlootchest.setup.menus.modules;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.functionals.ClickableButtons;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rotation {

    private static final List<String> POSTITIONS = Arrays.asList("HEAD", "BODY", "RIGHT_ARM", "LEFT_ARM", "RIGHT_LEG", "LEFT_LEG");


    public Rotation(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, ClickableButtons backAction) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing rotation...");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<Rotation>) () -> new Rotation(p, templateFile, name, path, session, backAction));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                (e) -> new RotationsMenu(p, templateFile, name, Utility.getPrevPath(path), session, backAction));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bPosition",
                        Arrays.asList(new String[]{
                                "&aCurrent value: " + "&7" + templateFile.getYml().getString(name + path + ".position", "null"),
                                "", "&eClick to set to new position!"}),
                        false),
                11,
                (e) -> {
                        if (!POSTITIONS.contains(templateFile.getYml().getString(name + path + ".position", "null"))) {
                            templateFile.setNotSave(name + path + ".position", POSTITIONS.get(0));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new position to: &e" + POSTITIONS.get(0)));
                            new Rotation(p, templateFile, name, path, session, backAction);

                        } else {
                            int i = POSTITIONS.indexOf(templateFile.getYml().getString(name + path + ".position", "null"));
                            if (i >= POSTITIONS.size()-1) {
                                i = -1;
                            }
                            templateFile.setNotSave(name + path + ".position", POSTITIONS.get(i+1));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new position to: &e" + POSTITIONS.get(i+1)));
                            new Rotation(p, templateFile, name, path, session, backAction);
                        }
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bRotate Value",
                        Arrays.asList(new String[]{
                                "&aCurrent value: " + "&7" + templateFile.getYml().getString(name + path + ".value", "null"),
                                "", "&eClick to set to new rotate value!"}),
                        false),
                12,
                (e) -> {
                    p.closeInventory();
                    final SetupSessions session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aThe format should be X, Y, Z", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        } else if (!Utility.isXYZFormat(input)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Format! Cancel the action!"));
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        }
                        sessions.end();
                        Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                () -> {
                                    templateFile.setNotSave(name + path + ".value", input);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + input));
                                    session2.getInvConstructor().createNew();
                                });
                    });
                });
        p.openInventory(inv);
    }

}

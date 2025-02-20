package org.twightlight.hlootchest.setup.menus.modules;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.TemplateMenu;
import org.twightlight.hlootchest.setup.menus.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.menus.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class Sound {
    public Sound(Player p, TConfigManager templateFile, String name, String path, SetupSessions session) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Settings");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<Settings>) () -> new Settings(p, templateFile, name, path, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                (e) -> new ButtonsMenu(p, templateFile, name, Utility.getPrevPath(path), session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bSound Type",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".sound", "null"),
                                "", "&eClick to set a new sound!"}),
                        false),
                11,
                (e) -> {
                    p.closeInventory();
                    final SetupSessions session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        } else if (!XSound.matchXSound(input).isPresent()) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Sound! Cancel the action!"));
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
                                    templateFile.setNotSave(name + path + ".sound", input);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new sound to: &e" + input));
                                    session2.getInvConstructor().createNew();
                                });
                    });
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bYaw",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".yaw", "null"),
                                "", "&eClick to set a new value!"}),
                        false),
                12,
                (e) -> {
                    p.closeInventory();
                    final SetupSessions session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        } else if (!Utility.isNumeric(input)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
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
                                    templateFile.setNotSave(name + path + ".yaw", input);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                    session2.getInvConstructor().createNew();
                                });
                    });
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bPitch",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".pitch", "null"),
                                "", "&eClick to set a new value!"}),
                        false),
                13,
                (e) -> {
                    p.closeInventory();
                    final SetupSessions session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        } else if (!Utility.isNumeric(input)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
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
                                    templateFile.setNotSave(name + path + ".pitch", input);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                    session2.getInvConstructor().createNew();
                                });
                    });
                });
        p.openInventory(inv);
    }
}

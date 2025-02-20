package org.twightlight.hlootchest.setup.menus.elements;

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
import org.twightlight.hlootchest.setup.menus.modules.Rotation;
import org.twightlight.hlootchest.setup.menus.modules.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class RotationsMenu {
    public RotationsMenu(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, ClickableButtons backAction) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing " + name + "...");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<RotationsMenu>) () -> new RotationsMenu(p, templateFile, name, path, session, backAction));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                backAction);
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Rotation", new ArrayList<>(), false),
                26,
                (e) -> {
                    p.closeInventory();
                    final SetupSessions session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the name of new rotation: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        session2.getInvConstructor().createNew();
                                    });
                            return;
                        }
                        if (templateFile.getYml().contains(name + path)) {
                            Set<String> rotList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
                            if (rotList.contains(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis rotation name already exist! Cancel the action!"));
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            session2.getInvConstructor().createNew();
                                        });
                                return;
                            }
                        }
                        sessions.end();
                        Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                () -> {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully created new rotation: &e"+ input));
                                    new Rotation(p, templateFile, name, path + "." + input, session, backAction);
                                });

                    });

                });
        int i = 0;
        if (templateFile.getYml().contains(name + path)) {
            Set<String> rotList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
            for (String rot : rotList) {
                MenuManager.setItem(p,
                        inv,
                        HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(),
                                "",
                                0,
                                "&eName: " + ChatColor.LIGHT_PURPLE + rot,
                                Arrays.asList(new String[] {"&aPosition: " + templateFile.getYml().getString(name + path + "." + rot + ".position", "null"),
                                        "&aValue: " + templateFile.getYml().getString(name + path + "." + rot + ".value" , "null"),
                                        "",
                                        "&eLeft-click to edit!",
                                        "&eRight-click to remove!"}),
                                false),
                        i,
                        (e) -> {
                            if (e.isLeftClick()) {
                                new Rotation(p, templateFile, name, path + "." + rot, session, backAction);
                            } else if (e.isRightClick()) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have successfully removed this rotation!"));
                                templateFile.getYml().set(name + path + "." + rot, null);
                                new RotationsMenu(p, templateFile, name, path, session, backAction);
                            }
                        });
                i ++;
            }
        }

        p.openInventory(inv);
    }
}

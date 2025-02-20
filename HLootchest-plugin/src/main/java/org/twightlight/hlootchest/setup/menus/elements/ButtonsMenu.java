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
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.TemplateMenu;
import org.twightlight.hlootchest.setup.menus.modules.Button;
import org.twightlight.hlootchest.setup.menus.modules.Rotation;
import org.twightlight.hlootchest.setup.menus.modules.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class ButtonsMenu {
    public ButtonsMenu(Player p, TConfigManager templateFile, String name, String path, SetupSessions session) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing " + name + "...");

        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }

        session.setInvConstructor((MenuHandler<ButtonsMenu>) () -> new ButtonsMenu(p, templateFile, name, path, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", new ArrayList<>(), false),
                18,
                (e) -> new TemplateMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Button", new ArrayList<>(), false),
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
                            Set<String> buttonList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
                            if (buttonList.contains(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis button name already exist! Cancel the action!"));
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
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully created new button: &e"+ input));
                                    new ButtonsMenu(p, templateFile, name, path + "." + input, session);
                                });

                    });

                });
        int i = 0;
        if (templateFile.getYml().contains(name + path)) {
            Set<String> buttonList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
            for (String button : buttonList) {
                MenuManager.setItem(p,
                        inv,
                        HLootchest.getNms().createItem(XMaterial.STONE_BUTTON.parseMaterial(),
                                "",
                                0,
                                "&eName: " + ChatColor.AQUA + button,
                                Arrays.asList(new String[] {
                                        "",
                                        "&eLeft-click to edit!",
                                        "&eRight-click to remove!"}),
                                false),
                        i,
                        (e) -> {
                            if (e.isLeftClick()) {
                                new Button(p, templateFile, name, path + "." + button, session);
                            } else if (e.isRightClick()) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have successfully removed this rotation!"));
                                templateFile.getYml().set(name + path + "." + button, null);
                                new ButtonsMenu(p, templateFile, name, path, session);
                            }
                        });
                i ++;
            }
        }

        p.openInventory(inv);
    }
}

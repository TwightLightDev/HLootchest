package org.twightlight.hlootchest.setup.elements;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.setup.modules.Button;
import org.twightlight.hlootchest.setup.modules.Reward;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class ChildrenMenu {
    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private final boolean isReward;


    public ChildrenMenu(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, boolean isReward) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.isReward = isReward;
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Children list");

        session.setInvConstructor((MenuHandler<ChildrenMenu>) () -> new ChildrenMenu(p, templateFile, name, path, session, isReward));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();

        if (isReward) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                    18,
                    (e) -> {
                        new Reward(p, templateFile, name, Utility.getPrevPath(path), session, false);
                    });
        } else {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                    18,
                    (e) -> {
                        new Button(p, templateFile, name, Utility.getPrevPath(path), session, false);
                    });
        }
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Child", Collections.emptyList(), false),
                26,
                (e) -> {
                    p.closeInventory();
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the name of the new child: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        setItems(inv);
                                    });
                            return;
                        }
                        if (templateFile.getYml().contains(name + path)) {
                            Set<String> buttonList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
                            if (buttonList.contains(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis child name already exist! Cancel the action!"));
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            }
                        }
                        sessions.end();
                        Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                () -> {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully created new child: &e"+ input));
                                    if (isReward) {
                                        new Reward(p, templateFile, name, path + "." + input, session, true);

                                    } else {
                                        new Button(p, templateFile, name, path + "." + input, session, true);
                                    }
                                });

                    });

                });
        int i = 0;
        if (templateFile.getYml().contains(name + path)) {
            Set<String> childList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
            for (String child : childList) {
                MenuManager.setItem(p,
                        inv,
                        HLootchest.getNms().createItem(XMaterial.STONE_BUTTON.parseMaterial(),
                                "",
                                0,
                                "&eName: " + ChatColor.AQUA + child,
                                Arrays.asList(new String[] {
                                        "",
                                        "&eLeft-click to edit!",
                                        "&eRight-click to remove!"}),
                                false),
                        i,
                        (e) -> {
                            if (e.isLeftClick()) {
                                if (isReward) {
                                    new Reward(p, templateFile, name, path + "." + child, session, true);

                                } else {
                                    new Button(p, templateFile, name, path + "." + child, session, true);
                                }
                            } else if (e.isRightClick()) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have successfully removed this child!"));
                                templateFile.getYml().set(name + path + "." + child, null);
                                setItems(inv);
                            }
                        });
                i ++;
            }
        }

        p.openInventory(inv);
    }
}

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
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.elements.*;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Reward {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private final boolean isChild;

    public Reward(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, boolean isChild) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.isChild = isChild;

        Inventory inv;
        if (isChild) {
            inv = Bukkit.createInventory(null, 54, ChatColor.GRAY + "Editing child...");
        } else {
            inv = Bukkit.createInventory(null, 54, ChatColor.GRAY + "Editing reward...");
        }

        session.setInvConstructor((MenuHandler<Reward>) () -> new Reward(p, templateFile, name, path, session, isChild));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();
        if (!isChild) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                    45,
                    (e) -> new RewardsMenu(p, templateFile, name, Utility.getPrevPath(path), session));
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                            "&bChance",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".chance", "null"),
                                    "", "&eClick to set a new value!"}),
                            false),
                    11,
                    (e) -> {
                        p.closeInventory();
                        ChatSessions sessions = new ChatSessions(p);
                        sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                            if (input.equals("cancel")) {
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            } else if (!Utility.isNumeric(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            }
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        templateFile.setNotSave(name + path + ".chance", Float.valueOf(input));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                            "&bClick sound",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    12,
                    (e) -> {
                        new Sound(p, templateFile, name, path + ".click-sound", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                            "&bHover sound",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    13,
                    (e) -> {
                        new Sound(p, templateFile, name, path + ".hover-sound", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
                            "&bRotations",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    14,
                    (e) -> {
                        new RotationsMenu(p, templateFile, name, path + ".rotations", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CLOCK.parseMaterial(), "", 0,
                            "&bDelay",
                            Arrays.asList(new String[]{"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".delay", "null"),
                                    "", "&eClick to set a new value!"}),
                            false),
                    15,
                    (e) -> {
                        p.closeInventory();
                        ChatSessions sessions = new ChatSessions(p);
                        sessions.prompt(Arrays.asList(new String[]{"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                            if (input.equals("cancel")) {
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            } else if (!Utility.isNumeric(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            }
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        templateFile.setNotSave(name + path + ".delay", Integer.valueOf(input));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.RED_WOOL.parseMaterial(), "", 0,
                            "&bSpawn Requirements",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    22,
                    (e) -> {
                        new RequirementsMenu(p, templateFile, name, path + ".spawn-requirements", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.RED_WOOL.parseMaterial(), "", 0,
                            "&bClick Requirements",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    23,
                    (e) -> {
                        new RequirementsMenu(p, templateFile, name, path + ".click-requirements", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
                            "&bRotate-on-spawn",
                            Arrays.asList(new String[]{"&aEnable: " + "&7" + templateFile.getBoolean(name + path + ".rotate-on-spawn.enable", false),
                                    "&aReverse: " + "&7" + templateFile.getBoolean(name + path + ".rotate-on-spawn.reverse", false),
                                    "&aFinal yaw: " + "&7" + templateFile.getString(name + path + ".rotate-on-spawn.final-yaw", "null"),
                                    "", "&eLeft-click to change 'Enable' option!",
                                    "&eRight-click to change 'Reverse' option!",
                                    "&eShift-left-click to change 'Final yaw' option!"}),
                            false),
                    24,
                    (e) -> {
                        if (e.isLeftClick() && e.isShiftClick()) {
                            p.closeInventory();
                            ChatSessions sessions = new ChatSessions(p);
                            sessions.prompt(Arrays.asList(new String[]{"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                                if (input.equals("cancel")) {
                                    sessions.end();
                                    Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                            () -> {
                                                setItems(inv);
                                            });
                                    return;
                                } else if (!Utility.isNumeric(input)) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
                                    sessions.end();
                                    Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                            () -> {
                                                setItems(inv);
                                            });
                                    return;
                                }
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            templateFile.setNotSave(name + path + ".rotate-on-spawn.final-yaw", Float.valueOf(input));
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new final yaw to: &e" + input));
                                            setItems(inv);
                                        });
                            });

                        } else if (e.isRightClick()) {
                            templateFile.setNotSave(name + path + ".rotate-on-spawn.reverse", !templateFile.getBoolean(name + path + ".rotate-on-spawn.reverse", false));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".rotate-on-spawn.reverse")));
                            setItems(inv);
                        } else if (e.isLeftClick()) {
                            templateFile.setNotSave(name + path + ".rotate-on-spawn.enable", !templateFile.getBoolean(name + path + ".rotate-on-spawn.enable", false));
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".rotate-on-spawn.enable")));
                            setItems(inv);
                        }
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.NAME_TAG.parseMaterial(), "", 0,
                            "&bDisplay Name Settings",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    29,
                    (e) -> {
                        new Name(p, templateFile, name, path + ".name", session, isChild,  (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });

            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "", 3,
                            "&bIcons",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    30,
                    (e) -> {
                        new Icon(p, templateFile, name, path + ".icon", session, isChild,  (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                            "&bChildren",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    31,
                    (e) -> {
                        new ChildrenMenu(p, templateFile, name, path + ".children", session, true);
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.SHEARS.parseMaterial(), "", 0,
                            "&bHolding Icon",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".holding-icon", true)),
                                    "", "&eClick to change!"}),
                            false),
                    32,
                    (e) -> {
                        templateFile.setNotSave(name + path + ".holding-icon", !templateFile.getBoolean(name + path + ".holding-icon", true));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".holding-icon")));
                        setItems(inv);
                    });
            List<String> actions = Collections.emptyList();
            if (templateFile.getYml().contains(name + path + ".actions")) {
                actions = templateFile.getList(name + path + ".actions");
            }
            actions = actions.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', "&f" + "- " + line))
                    .collect(Collectors.toList());
            actions.add("");
            actions.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add an action."));
            actions.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last action."));
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(
                            XMaterial.COMMAND_BLOCK.parseMaterial(),
                            "",
                            0,
                            "&bActions",
                            actions,
                            false),
                    20,
                    (e) -> {
                        List<String> actions1 = Collections.emptyList();
                        if (templateFile.getYml().contains(name + path + ".actions")) {
                            actions1 = templateFile.getList(name + path + ".actions");
                        }
                        if (e.isLeftClick()) {
                            p.closeInventory();
                            final List<String> actions2 = actions1;
                            ChatSessions sessions = new ChatSessions(p);
                            sessions.prompt(Arrays.asList(new String[]{"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                                if (input.equals("cancel")) {
                                    sessions.end();
                                    Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                            () -> {
                                                setItems(inv);
                                            });
                                    return;
                                }
                                actions2.add(input);
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            templateFile.setNotSave(name + path + ".actions", actions2);
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully added a new action: &e" + input));
                                            setItems(inv);
                                        });
                            });
                        } else if (e.isRightClick()) {
                            actions1.remove(actions1.size() - 1);
                            templateFile.setNotSave(name + path + ".actions", actions1);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully removed this action! "));
                            setItems(inv);
                        }
                    });
            List<String> rewards = Collections.emptyList();
            if (templateFile.getYml().contains(name + path + ".rewards")) {
                rewards = templateFile.getList(name + path + ".rewards");
            }
            rewards = rewards.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', "&f" + "- " + line))
                    .collect(Collectors.toList());
            rewards.add("");
            rewards.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add a reward."));
            rewards.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last reward."));
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(
                            XMaterial.COMMAND_BLOCK.parseMaterial(),
                            "",
                            0,
                            "&bRewards",
                            rewards,
                            false),
                    21,
                    (e) -> {
                        List<String> rewards1 = Collections.emptyList();
                        if (templateFile.getYml().contains(name + path + ".rewards")) {
                            rewards1 = templateFile.getList(name + path + ".rewards");
                        }
                        if (e.isLeftClick()) {
                            p.closeInventory();
                            final List<String> rewards2 = rewards1;
                            ChatSessions sessions = new ChatSessions(p);
                            sessions.prompt(Arrays.asList(new String[]{"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                                if (input.equals("cancel")) {
                                    sessions.end();
                                    Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                            () -> {
                                                setItems(inv);
                                            });
                                    return;
                                }
                                rewards2.add(input);
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            templateFile.setNotSave(name + path + ".rewards", rewards2);
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully added a new reward: &e" + input));
                                            setItems(inv);
                                        });
                            });
                        } else if (e.isRightClick()) {
                            rewards1.remove(rewards1.size() - 1);
                            templateFile.setNotSave(name + path + ".rewards", rewards1);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully removed this reward! "));
                            setItems(inv);
                        }
                    });
        } else {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                    45,
                    (e) -> new ChildrenMenu(p, templateFile, name, Utility.getPrevPath(path), session, true));
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                            "&bLocation",
                            Arrays.asList(new String[]{"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".location", "null"),
                                    "", "&eClick to set to your current location!"}),
                            false),
                    11,
                    (e) -> {
                        templateFile.setNotSave(name + path + ".location", Utility.locationToString(p.getLocation()));
                        templateFile.setNotSave(name + path + ".location-offset", null);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation())));
                        setItems(inv);
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                            "&bLocation offset",
                            Arrays.asList(new String[]{
                                    "&aCurrent value: " + "&7" + templateFile.getYml().getString(name + path + ".location-offset", "null"),
                                    "", "&eClick to set to new offset value!"}),
                            false),
                    12,
                    (e) -> {
                        p.closeInventory();
                        final SetupSessions session2 = session;
                        ChatSessions sessions = new ChatSessions(p);
                        sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aLocation-offset support math!", "&aType 'cancel' to cancel!"}), (input) -> {
                            if (input.equals("cancel")) {
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            }
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        templateFile.setNotSave(name + path + ".location-offset", input);
                                        templateFile.setNotSave(name + path + ".location", null);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.COMPASS.parseMaterial(), "", 0,
                            "&bRotations",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    13,
                    (e) -> {
                        new RotationsMenu(p, templateFile, name, path + ".rotations", session, (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.NAME_TAG.parseMaterial(), "", 0,
                            "&bDisplay Name Settings",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    14,
                    (e) -> {
                        new Name(p, templateFile, name, path + ".name", session, isChild,  (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });

            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "", 3,
                            "&bIcons",
                            Arrays.asList(new String[]{"&eClick to browse!"}),
                            false),
                    15,
                    (e) -> {
                        new Icon(p, templateFile, name, path + ".icon", session, isChild,  (ev) -> new Reward(p, templateFile, name, path, session, isChild));
                    });
        }
        p.openInventory(inv);
    }
}

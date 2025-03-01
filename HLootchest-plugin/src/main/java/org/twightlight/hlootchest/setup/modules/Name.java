package org.twightlight.hlootchest.setup.modules;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSessions;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Name {
    private static final List<String> NAME_MODE = Arrays.asList("always", "hover");

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private final boolean isChild;
    private final Executable backAction;
    public Name(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, boolean isChild, Executable backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.isChild = isChild;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing button...");

        session.setInvConstructor((MenuHandler<Name>) () -> new Name(p, templateFile, name, path, session, isChild, backAction));
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
                backAction);
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.NAME_TAG.parseMaterial(), "", 0,
                        "&bEnable",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".enable", false)),
                                "", "&eClick to change!"}),
                        false),
                11,
                (e) -> {
                        templateFile.setNotSave(name + path + ".enable", !templateFile.getBoolean(name + path + ".enable", false));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".enable")));
                        setItems(inv);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bVisible mode",
                        Arrays.asList(new String[]{
                                "&aCurrent value: " + "&7" + templateFile.getYml().getString(name + path + ".visible-mode", "null"),
                                "", "&eClick to set to switch mode!"}),
                        false),
                12,
                (e) -> {
                    if (!NAME_MODE.contains(templateFile.getYml().getString(name + path + ".visible-mode", "always"))) {
                        templateFile.setNotSave(name + path + ".visible-mode", NAME_MODE.get(0));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully switch mode to: &e" + NAME_MODE.get(0)));
                        setItems(inv);

                    } else {
                        int i = NAME_MODE.indexOf(templateFile.getYml().getString(name + path + ".visible-mode", "always"));
                        if (i >= NAME_MODE.size()-1) {
                            i = -1;
                        }
                        templateFile.setNotSave(name + path + ".visible-mode", NAME_MODE.get(i+1));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully switch mode to: &e" + NAME_MODE.get(i+1)));
                        setItems(inv);
                    }
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.NAME_TAG.parseMaterial(), "", 0,
                        "&bDynamic",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".dynamic", false)),
                                "", "&eClick to change!"}),
                        true),
                13,
                (e) -> {
                    templateFile.setNotSave(name + path + ".dynamic", !templateFile.getBoolean(name + path + ".dynamic", false));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".dynamic")));
                    setItems(inv);
                });
        boolean dynamic = templateFile.getBoolean(name + path + ".dynamic", false);
        if (dynamic) {
            List<String> display_name = Collections.emptyList();
            if (templateFile.getYml().contains(name + path + ".display-name")) {
                display_name = templateFile.getList(name + path + ".display-name");
            }
            display_name = display_name.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', "&f" + "- " + line))
                    .collect(Collectors.toList());
            display_name.add("");
            display_name.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add a new line."));
            display_name.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last line."));
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(
                            XMaterial.FEATHER.parseMaterial(),
                            "",
                            0,
                            "&bDisplay Name",
                            display_name,
                            false),
                    14,
                    (e) -> {
                        List<String> display_name1 = Collections.emptyList();
                        if (templateFile.getYml().contains(name + path + ".display-name")) {
                            display_name1 = templateFile.getList(name + path  + ".display-name");
                        }
                        if (e.isLeftClick()) {
                            p.closeInventory();
                            final List<String> display_name2 = display_name1;
                            ChatSessions sessions = new ChatSessions(p);
                            sessions.prompt(Arrays.asList(new String[] {"&aType the value you want: ", "&aType 'cancel' to cancel!"}), (input) -> {
                                if (input.equals("cancel")) {
                                    sessions.end();
                                    Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                            () -> {
                                                setItems(inv);
                                            });
                                    return;
                                }
                                display_name2.add(input);
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                        () -> {
                                            templateFile.setNotSave(name + path + ".display-name", display_name2);
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully added a new line: &e" + input));
                                            setItems(inv);
                                        });
                            });
                        } else if (e.isRightClick()) {
                            display_name1.remove(display_name1.size()-1);
                            templateFile.setNotSave(name + path  + ".display-name", display_name1);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully removed this line! "));
                            setItems(inv);
                        }
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CLOCK.parseMaterial(), "", 0,
                            "&bRefresh interval",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".refresh-interval", "null"),
                                    "", "&eClick to set a new value!"}),
                            false),
                    15,
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
                                        templateFile.setNotSave(name + path + ".refresh-interval", Float.valueOf(input).intValue());
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });


        } else {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.FEATHER.parseMaterial(), "", 0,
                            "&bDisplay Name",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".display-name", "null"),
                                    "", "&eClick to set a new name!"}),
                            false),
                    14,
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
                            }
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        templateFile.setNotSave(name + path + ".display-name", input);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new name to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });
        }
        p.openInventory(inv);
    }
}

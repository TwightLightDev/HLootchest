package org.twightlight.hlootchest.setup.modules;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class IconSettings {

    private static final List<String> SLOT = Arrays.asList("HEAD", "CHESTPLATE", "LEGGINGS", "BOOTS", "MAIN_HAND", "OFF_HAND");

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;
    private final Executable backAction;

    public IconSettings(Player p, TConfigManager templateFile, String name, String path, SetupSession session, Executable backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing icon...");


        session.setInvConstructor((MenuHandler<IconSettings>) () -> new IconSettings(p, templateFile, name, path, session, backAction));
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
                HLootchest.getNms().createItem(XMaterial.valueOf(templateFile.getString(name + path+ ".material", "BEDROCK")).parseMaterial(),
                        templateFile.getString(name + path + ".head_value", ""),
                        templateFile.getInt(name + path + ".data", 0),
                        "&bMaterial",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".material", "null"),
                                "", "&eClick to set a new material!"}),
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
                        } else if (!XMaterial.matchXMaterial(input).isPresent()) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Material! Cancel the action!"));
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
                                    templateFile.setNotSave(name + path + ".material", input);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new material to: &e" + input));
                                    setItems(inv);
                                });
                    });
                });

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.YELLOW_WOOL.parseMaterial(),
                        "",
                        0,
                        "&bData",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".data", "null"),
                                "", "&eClick to set data!"}),
                        false),
                12,
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
                                    templateFile.setNotSave(name + path + ".data", Float.valueOf(input).intValue());
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set data to: &e" + input));
                                    setItems(inv);
                                });
                    });
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.GLOWSTONE_DUST.parseMaterial(), "", 0,
                        "&bGlowing",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".glowing", false)),
                                "", "&eClick to change!"}),
                        templateFile.getBoolean(name + path + ".glowing", false)),
                13,
                (e) -> {
                    templateFile.setNotSave(name + path + ".glowing", !templateFile.getBoolean(name + path + ".glowing", false));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".glowing")));
                    setItems(inv);
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARMOR_STAND.parseMaterial(), "", 0,
                        "&bSlot",
                        Arrays.asList(new String[]{
                                "&aCurrent value: " + "&7" + templateFile.getYml().getString(name + path + ".slot", "null"),
                                "", "&eClick to set to new slot!"}),
                        false),
                14,
                (e) -> {
                    if (!SLOT.contains(templateFile.getYml().getString(name + path + ".slot", "null"))) {
                        templateFile.setNotSave(name + path + ".slot", SLOT.get(0));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new slot to: &e" + SLOT.get(0)));
                        setItems(inv);

                    } else {
                        int i = SLOT.indexOf(templateFile.getYml().getString(name + path + ".slot", "null"));
                        if (i >= SLOT.size()-1) {
                            i = -1;
                        }
                        templateFile.setNotSave(name + path + ".slot", SLOT.get(i+1));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new slot to: &e" + SLOT.get(i+1)));
                        setItems(inv);
                    }
                });

        if (XMaterial.PLAYER_HEAD == XMaterial.valueOf(templateFile.getString(name + path+ ".material", "BEDROCK"))) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.NAME_TAG.parseMaterial(),
                            "",
                            0,
                            "&bHead value",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".head_value", "null"),
                                    "", "&eClick to set new value!"}),
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
                            }
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        templateFile.setNotSave(name + path + ".head_value", input);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + input));
                                        setItems(inv);
                                    });
                        });
                    });
        }
        p.openInventory(inv);
    }
}

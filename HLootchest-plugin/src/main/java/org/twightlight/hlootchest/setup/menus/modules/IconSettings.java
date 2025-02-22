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
import org.twightlight.hlootchest.setup.functionals.ClickableButtons;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;


public class IconSettings {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private final ClickableButtons backAction;

    public IconSettings(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, ClickableButtons backAction) {
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
                    13,
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

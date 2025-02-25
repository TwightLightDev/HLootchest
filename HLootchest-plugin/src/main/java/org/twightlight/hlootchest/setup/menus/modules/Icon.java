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
import org.twightlight.hlootchest.setup.functionals.ClickableAction;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.setup.menus.elements.IconsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;

public class Icon {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private boolean isChild;
    private final ClickableAction backAction;
    public Icon(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, boolean isChild, ClickableAction backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.isChild = isChild;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing icon...");

        session.setInvConstructor((MenuHandler<Icon>) () -> new Icon(p, templateFile, name, path, session, isChild, backAction));
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
                        "&bDynamic",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + String.valueOf(templateFile.getBoolean(name + path + ".dynamic", false)),
                                "", "&eClick to change!"}),
                        true),
                11,
                (e) -> {
                    templateFile.setNotSave(name + path + ".dynamic", !templateFile.getBoolean(name + path + ".dynamic", false));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new value to: &e" + templateFile.getBoolean(name + path + ".dynamic")));
                    setItems(inv);
                });
        boolean dynamic = templateFile.getBoolean(name + path + ".dynamic", false);
        if (dynamic) {
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "", 0,
                            "&bIcon List",
                            Arrays.asList(new String[] {"&eClick to browse!"}),
                            false),
                    12,
                    (e) -> {
                        new IconsMenu(p, templateFile, name, path+".dynamic-icons", session, isChild, backAction);
                    });
            MenuManager.setItem(p,
                    inv,
                    HLootchest.getNms().createItem(XMaterial.CLOCK.parseMaterial(), "", 0,
                            "&bRefresh interval",
                            Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + path + ".refresh-interval", "null"),
                                    "", "&eClick to set a new value!"}),
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
                    HLootchest.getNms().createItem(XMaterial.valueOf(templateFile.getString(name + path+ ".material", "BEDROCK")).parseMaterial(),
                    templateFile.getString(name + path + ".head_value", ""),
                    templateFile.getInt(name + path + ".data", 0),
                            "&bIcon Settings",
                            Arrays.asList(new String[] {"&eClick to browse!"}),
                            false),
                    12,
                    (e) -> {
                        new IconSettings(p, templateFile, name, path, session, (ev) -> new Icon(p, templateFile, name, path, session, isChild, backAction));
                    });
        }
        p.openInventory(inv);
    }
}

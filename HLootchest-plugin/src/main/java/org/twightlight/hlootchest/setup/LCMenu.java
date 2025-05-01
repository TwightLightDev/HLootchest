package org.twightlight.hlootchest.setup;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.elements.RewardsMenu;
import org.twightlight.hlootchest.setup.modules.IconSettings;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;

public class LCMenu {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final SetupSession session;


    public LCMenu(Player p, TConfigManager templateFile, String name, SetupSession session) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.session = session;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Editing " + name + "...");

        session.setInvConstructor((MenuHandler<LCMenu>) () -> new LCMenu(p, templateFile, name, session));
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
                (e) -> new LCSMainMenu(p));

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.valueOf(templateFile.getString(name + ".icon.material", "BEDROCK")).parseMaterial(),
                        templateFile.getString(name + ".icon.head_value", ""),
                        templateFile.getInt(name + ".icon.data", 0),
                        "&bIcon",
                        Arrays.asList(new String[] {"&aClick to browse!"}),
                        false),
                11,
                (e) -> new IconSettings(p, templateFile, name, ".icon", session, (ev) -> new LCMenu(p, templateFile, name, session)));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.CHEST.parseMaterial(), "", 0,
                        "&bRewards amount",
                        Arrays.asList(new String[] {"&aCurrent value: " + "&7" + templateFile.getString(name + ".reward-amount", "null"),
                                "", "&eClick to set a new value!"}),
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
                                    templateFile.setNotSave(name + ".reward-amount", Float.valueOf(input).intValue());
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set value to: &e" + input));
                                    setItems(inv);
                                });
                    });
                });
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(
                        XMaterial.EMERALD.parseMaterial(),
                        "",
                        0,
                        ChatColor.YELLOW + "Rewards",
                        Arrays.asList(new String[] {"&aClick to browse!"}),
                        false),
                13,
                (e) -> new RewardsMenu(p, templateFile, name, ".rewards-list", session));

        p.openInventory(inv);
    }
}


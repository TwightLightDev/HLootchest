package org.twightlight.hlootchest.setup.elements;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.setup.modules.Rotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class RotationsMenu {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;
    private final Executable backAction;


    public RotationsMenu(Player p, TConfigManager templateFile, String name, String path, SetupSession session, Executable backAction) {

        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Rotations");

        session.setInvConstructor((MenuHandler<RotationsMenu>) () -> new RotationsMenu(p, templateFile, name, path, session, backAction));
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
                HLootchest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Rotation", Collections.emptyList(), false),
                26,
                (e) -> {
                    p.closeInventory();
                    final SetupSession session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the name of new rotation: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        setItems(inv);
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
                                            setItems(inv);
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
                                setItems(inv);
                            }
                        });
                i ++;
            }
        }

        p.openInventory(inv);
    }
}

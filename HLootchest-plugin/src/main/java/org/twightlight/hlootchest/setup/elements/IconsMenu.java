package org.twightlight.hlootchest.setup.elements;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.setup.modules.IconSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class IconsMenu {

    private final Player p;
    private final TYamlWrapper templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;
    private boolean isChild;
    private Executable backAction;
    public IconsMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild, Executable backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.isChild = isChild;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Icons");

        session.setInvConstructor((MenuHandler<IconsMenu>) () -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                18,
                backAction);
        MenuManager.setItem(p,
                inv,
                HLootChest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Icon", Collections.emptyList(), false),
                26,
                (e) -> {
                    p.closeInventory();
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the name of the new button: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootChest.getInstance(),
                                    () -> {
                                        setItems(inv);
                                    });
                            return;
                        }
                        if (templateFile.getYml().contains(name + path)) {
                            Set<String> buttonList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
                            if (buttonList.contains(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis icon name already exist! Cancel the action!"));
                                sessions.end();
                                Bukkit.getScheduler().runTask(HLootChest.getInstance(),
                                        () -> {
                                            setItems(inv);
                                        });
                                return;
                            }
                        }
                        sessions.end();
                        Bukkit.getScheduler().runTask(HLootChest.getInstance(),
                                () -> {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully created new icon: &e"+ input));
                                    new IconSettings(p, templateFile, name, path + "." + input, session, (ev) -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
                                });

                    });

                });
        int i = 0;
        if (templateFile.getYml().contains(name + path)) {
            Set<String> iconsList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
            for (String icon : iconsList) {
                MenuManager.setItem(p,
                        inv,
                        HLootChest.getNms().createItem(XMaterial.valueOf(templateFile.getString(name + path + "." + icon + ".material", "BEDROCK")).parseMaterial(),
                                templateFile.getString(name + path + "." + icon + ".head_value", ""),
                                templateFile.getInt(name + path + "." + icon + ".data", 0),
                                "&eName: " + ChatColor.AQUA + icon,
                                Arrays.asList(new String[] {
                                        "",
                                        "&eLeft-click to edit!",
                                        "&eRight-click to remove!"}),
                                false),
                        i,
                        (e) -> {
                            if (e.isLeftClick()) {
                                new IconSettings(p, templateFile, name + "." + icon, path, session, (ev) -> new IconsMenu(p, templateFile, name, path, session, isChild, backAction));
                            } else if (e.isRightClick()) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have successfully removed this icon!"));
                                templateFile.getYml().set(name + path + "." + icon, null);
                                setItems(inv);
                            }
                        });
                i ++;
            }
        }

        p.openInventory(inv);
    }

}

package org.twightlight.hlootchest.setup.elements;

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
import org.twightlight.hlootchest.setup.LCMenu;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.setup.modules.Reward;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class RewardsMenu {

    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;

    public RewardsMenu(Player p, TConfigManager templateFile, String name, String path, SetupSession session) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Rewards list");

        session.setInvConstructor((MenuHandler<RewardsMenu>) () -> new RewardsMenu(p, templateFile, name, path, session));
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
                (e) -> new LCMenu(p, templateFile, name, session));
        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.SLIME_BALL.parseMaterial(), "", 0, ChatColor.GREEN + "Add New Reward", Collections.emptyList(), false),
                26,
                (e) -> {
                    p.closeInventory();
                    final SetupSession session2 = session;
                    ChatSessions sessions = new ChatSessions(p);
                    sessions.prompt(Arrays.asList(new String[] {"&aType the name of the new reward: ", "&aType 'cancel' to cancel!"}), (input) -> {
                        if (input.equals("cancel")) {
                            sessions.end();
                            Bukkit.getScheduler().runTask(HLootchest.getInstance(),
                                    () -> {
                                        setItems(inv);
                                    });
                            return;
                        }
                        if (templateFile.getYml().contains(name + path)) {
                            Set<String> rewardsList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
                            if (rewardsList.contains(input)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis reward name already exist! Cancel the action!"));
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
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou successfully created new reward: &e"+ input));
                                    new Reward(p, templateFile, name, path + "." + input, session, false);
                                });

                    });

                });
        int i = 0;
        if (templateFile.getYml().contains(name + path)) {
            Set<String> rewardsList = templateFile.getYml().getConfigurationSection(name + path).getKeys(false);
            for (String reward : rewardsList) {
                MenuManager.setItem(p,
                        inv,
                        HLootchest.getNms().createItem(XMaterial.EMERALD.parseMaterial(),
                                "",
                                0,
                                "&eName: " + ChatColor.AQUA + reward,
                                Arrays.asList(new String[] {
                                        "",
                                        "&eLeft-click to edit!",
                                        "&eRight-click to remove!"}),
                                false),
                        i,
                        (e) -> {
                            if (e.isLeftClick()) {
                                new Reward(p, templateFile, name, path + "." + reward, session, false);
                            } else if (e.isRightClick()) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have successfully removed this reward!"));
                                templateFile.getYml().set(name + path + "." + reward, null);
                                setItems(inv);
                            }
                        });
                i ++;
            }
        }
        p.openInventory(inv);
    }
}

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
import org.twightlight.hlootchest.setup.functionals.MenuHandler;
import org.twightlight.hlootchest.setup.menus.MenuManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;

public class Sound {
    private final Player p;
    private final TConfigManager templateFile;
    private final String name;
    private final String path;
    private final SetupSessions session;
    private final Inventory inv;
    private static final int SOUND_SLOT = 11;
    private static final int YAW_SLOT = 12;
    private static final int PITCH_SLOT = 13;
    private final ClickableButtons backAction;

    public Sound(Player p, TConfigManager templateFile, String name, String path, SetupSessions session, ClickableButtons backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.backAction = backAction;
        this.inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Settings");

        session.setInvConstructor((MenuHandler<Sound>) () -> new Sound(p, templateFile, name, path, session, backAction));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        MenuManager.removeData(p);
        inv.clear();

        MenuManager.setItem(p,
                inv,
                HLootchest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false),
                18,
                backAction);
        addConfigurableItem(SOUND_SLOT, "sound", "&bSound Type", (input) -> XSound.matchXSound(input).isPresent());
        addConfigurableItem(YAW_SLOT, "yaw", "&bYaw", Utility::isNumeric);
        addConfigurableItem(PITCH_SLOT, "pitch", "&bPitch", Utility::isNumeric);

        p.openInventory(inv);
    }

    private void addItem(int slot, XMaterial material, String displayName, String value,ClickableButtons action) {
        MenuManager.setItem(
                p,
                inv,
                HLootchest.getNms().createItem(material.parseMaterial(), "", 0, ChatColor.translateAlternateColorCodes('&', displayName), Arrays.asList("&aCurrent value: &7" + value, "","&eClick to set a new value!"), false),
                slot,
                action
        );
    }

    private void addConfigurableItem(int slot, String key, String displayName, java.util.function.Function<String, Boolean> validator) {
        String currentValue = templateFile.getString(name + path + "." + key, "null");
        addItem(slot, XMaterial.CHEST, displayName, currentValue , e -> {
            p.closeInventory();
            ChatSessions chatSession = new ChatSessions(p);
            chatSession.prompt(Arrays.asList("&aType the value you want:", "&aType 'cancel' to cancel!"), input -> {
                if ("cancel".equalsIgnoreCase(input)) {
                    chatSession.end();
                } else if (!validator.apply(input)) {
                    p.sendMessage(ChatColor.RED + "Invalid Value! Action canceled.");
                } else {
                    if ("yaw".equals(key) || "pitch".equals(key)) {
                        templateFile.setNotSave(name + path + "." + key, Float.valueOf(input).intValue());
                    } else {
                        templateFile.setNotSave(name + path + "." + key, input);
                    }
                    p.sendMessage(ChatColor.GREEN + "Successfully set " + key + " to: " + ChatColor.YELLOW + input);
                }
                chatSession.end();
                Bukkit.getScheduler().runTask(HLootchest.getInstance(), () -> setItems(inv));
            });
        });
    }
}
package org.twightlight.hlootchest.setup.modules;

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
import org.twightlight.hlootchest.setup.elements.RequirementsMenu;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Requirement {
    private static final List<String> TYPES = Arrays.asList("has-permission", "string-equals", ">=", ">", "==", "<", "<=", "!=");
    private final Player p;
    private final TYamlWrapper templateFile;
    private final String name;
    private final String path;
    private final SetupSession session;
    private final Executable backAction;


    public Requirement(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
        this.backAction = backAction;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Requirement");
        session.setInvConstructor((MenuHandler<Requirement>) () -> new Requirement(p, templateFile, name, path, session, backAction));
        setItems(inv);
    }

    private void setItems(Inventory inv) {
        if (MenuManager.getButtonsList().containsKey(p.getUniqueId())) {
            MenuManager.removeData(p);
        }
        inv.clear();

        MenuManager.setItem(p, inv, HLootChest.getNms().createItem(XMaterial.ARROW.parseMaterial(), "", 0, ChatColor.GREEN + "Back", Collections.emptyList(), false), 18,
                (e) -> new RequirementsMenu(p, templateFile, name, Utility.getPrevPath(path), session, backAction));

        MenuManager.setItem(p, inv, HLootChest.getNms().createItem(XMaterial.COMPARATOR.parseMaterial(), "", 0,
                        "&bType", Arrays.asList("&aCurrent value: &7" + templateFile.getYml().getString(name + path + ".type", "null"), "", "&eClick to set to new type!"), false),
                11, (e) -> cycleRequirementType(inv));

        String type = templateFile.getYml().getString(name + path + ".type", "null");

        if ("has-permission".equals(type)) {
            setRequirementValue(inv, "value", "&bValue");
        } else if (!"null".equals(type)) {
            setRequirementValue(inv, "input", "&bInput");
            setRequirementValue(inv, "output", "&bOutput");
        }

        p.openInventory(inv);
    }

    private void cycleRequirementType(Inventory inv) {
        String currentType = templateFile.getYml().getString(name + path + ".type", "null");
        int index = TYPES.indexOf(currentType);
        index = (index == -1 || index >= TYPES.size() - 1) ? 0 : index + 1;

        String newType = TYPES.get(index);
        templateFile.setNotSave(name + path + ".type", newType);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new type to: &e" + newType));
        setItems(inv);
    }

    private void setRequirementValue(Inventory inv, String key, String displayName) {
        MenuManager.setItem(p, inv, HLootChest.getNms().createItem(XMaterial.FLINT.parseMaterial(), "", 0, displayName,
                        Arrays.asList("&aCurrent value: &7" + templateFile.getString(name + path + "." + key, "null"), "", "&eClick to set a new " + key + "!"), false),
                key.equals("input") ? 12 : 13, (e) -> handleChatInput(inv, key));
    }

    private void handleChatInput(Inventory inv, String key) {
        p.closeInventory();
        ChatSessions sessions = new ChatSessions(p);
        sessions.prompt(Arrays.asList("&aType the value you want: ", "&aType 'cancel' to cancel!"), (input) -> {
            if ("cancel".equalsIgnoreCase(input)) {
                sessions.end();
                Bukkit.getScheduler().runTask(HLootChest.getInstance(), () -> setItems(inv));
                return;
            }

            Object finalInput = input;
            String type = templateFile.getYml().getString(name + path + ".type", "null");
            if (!"string-equals".equals(type) && !Utility.isNumeric(input)) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Type! Cancel the action!"));
                sessions.end();
                Bukkit.getScheduler().runTask(HLootChest.getInstance(), () -> setItems(inv));
                return;
            }

            if (!"string-equals".equals(type)) {
                finalInput = Float.valueOf(input);
            }

            templateFile.setNotSave(name + path + "." + key, finalInput);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully set new " + key + " to: &e" + input));
            sessions.end();
            Bukkit.getScheduler().runTask(HLootChest.getInstance(), () -> setItems(inv));
        });
    }
}

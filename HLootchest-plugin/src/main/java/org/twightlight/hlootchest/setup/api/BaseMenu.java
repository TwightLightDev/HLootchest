package org.twightlight.hlootchest.setup.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.MenuManager;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseMenu {

    protected final Player p;
    protected final TYamlWrapper templateFile;
    protected final String name;
    protected final String path;
    protected final SetupSession session;
    protected Inventory inv;

    protected BaseMenu(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session) {
        this.p = p;
        this.templateFile = templateFile;
        this.name = name;
        this.path = path;
        this.session = session;
    }

    protected final void open(int size, String title, MenuHandler<?> constructor) {
        this.inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        session.setInvConstructor(constructor);
        buildAndOpen();
    }

    protected final void buildAndOpen() {
        MenuManager.removeData(p);
        inv.clear();
        populate();
        p.openInventory(inv);
    }

    protected abstract void populate();

    protected final String fullPath(String suffix) {
        return name + path + suffix;
    }

    protected final String fullPath() {
        return name + path;
    }

    protected final void item(int slot, XMaterial mat, String displayName, List<String> lore, Executable action) {
        MenuManager.setItem(p, inv, HLootChest.getNms().createItem(
                mat.parseMaterial(), "", 0, displayName, lore, false), slot, action);
    }

    protected final void item(int slot, XMaterial mat, String headValue, int data,
                              String displayName, List<String> lore, boolean glow, Executable action) {
        MenuManager.setItem(p, inv, HLootChest.getNms().createItem(
                mat.parseMaterial(), headValue, data, displayName, lore, glow), slot, action);
    }

    protected final void backButton(int slot, Executable action) {
        item(slot, XMaterial.ARROW, ChatColor.GREEN + "Back", Collections.emptyList(), action);
    }

    protected final void toggleItem(int slot, XMaterial mat, String displayName, String configKey, boolean def) {
        boolean current = templateFile.getBoolean(fullPath(configKey), def);
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + current, "", "&eClick to change!"),
                e -> {
                    templateFile.setNotSave(fullPath(configKey), !current);
                    msg("&aSuccessfully set new value to: &e" + !current);
                    buildAndOpen();
                });
    }

    protected final void locationItem(int slot, String displayName, String configKey) {
        item(slot, XMaterial.ARMOR_STAND, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath(configKey), "null"),
                        "", "&eClick to set to your current location!"),
                e -> {
                    templateFile.setNotSave(fullPath(configKey), Utility.locationToString(p.getLocation()));
                    msg("&aSuccessfully set new value to: &e" + Utility.locationToString(p.getLocation()));
                    buildAndOpen();
                });
    }

    protected final void numericChatItem(int slot, XMaterial mat, String displayName, String configKey) {
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath(configKey), "null"),
                        "", "&eClick to set a new value!"),
                e -> ChatPrompt.promptNumeric(p, this::buildAndOpen, input -> {
                    templateFile.setNotSave(fullPath(configKey), Float.valueOf(input).intValue());
                    msg("&aSuccessfully set value to: &e" + input);
                    buildAndOpen();
                }));
    }

    protected final void numericChatItemFloat(int slot, XMaterial mat, String displayName, String configKey) {
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath(configKey), "null"),
                        "", "&eClick to set a new value!"),
                e -> ChatPrompt.promptNumeric(p, this::buildAndOpen, input -> {
                    templateFile.setNotSave(fullPath(configKey), Float.valueOf(input));
                    msg("&aSuccessfully set value to: &e" + input);
                    buildAndOpen();
                }));
    }

    protected final void stringChatItem(int slot, XMaterial mat, String displayName, String configKey) {
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath(configKey), "null"),
                        "", "&eClick to set a new value!"),
                e -> ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    templateFile.setNotSave(fullPath(configKey), input);
                    msg("&aSuccessfully set new value to: &e" + input);
                    buildAndOpen();
                }));
    }

    protected final void validatedChatItem(int slot, XMaterial mat, String displayName, String configKey,
                                           java.util.function.Function<String, Boolean> validator, String errorMsg) {
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath(configKey), "null"),
                        "", "&eClick to set a new value!"),
                e -> ChatPrompt.prompt(p,
                        Arrays.asList("&aType the value you want: ", "&aType 'cancel' to cancel!"),
                        input -> {
                            if (!validator.apply(input)) {
                                msg(errorMsg);
                                return false;
                            }
                            return true;
                        }, this::buildAndOpen, input -> {
                            templateFile.setNotSave(fullPath(configKey), input);
                            msg("&aSuccessfully set new value to: &e" + input);
                            buildAndOpen();
                        }));
    }

    protected final void cycleItem(int slot, XMaterial mat, String displayName, String configKey,
                                   List<String> options, String defaultVal) {
        String current = templateFile.getYml().getString(fullPath(configKey), defaultVal);
        item(slot, mat, displayName,
                Arrays.asList("&aCurrent value: &7" + current, "", "&eClick to switch!"),
                e -> {
                    int i = options.indexOf(current);
                    String next = options.get((i + 1) % options.size());
                    templateFile.setNotSave(fullPath(configKey), next);
                    msg("&aSuccessfully set to: &e" + next);
                    buildAndOpen();
                });
    }

    protected final void editableListItem(int slot, XMaterial mat, String displayName, String configKey) {
        List<String> list = templateFile.getYml().contains(fullPath(configKey))
                ? templateFile.getList(fullPath(configKey)) : Collections.emptyList();
        List<String> lore = list.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', "&f- " + line))
                .collect(Collectors.toList());
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add."));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last entry."));
        item(slot, mat, displayName, lore, e -> {
            List<String> current = templateFile.getYml().contains(fullPath(configKey))
                    ? templateFile.getList(fullPath(configKey)) : Collections.emptyList();
            if (e.isLeftClick()) {
                ChatPrompt.promptString(p, this::buildAndOpen, input -> {
                    current.add(input);
                    templateFile.setNotSave(fullPath(configKey), current);
                    msg("&aSuccessfully added: &e" + input);
                    buildAndOpen();
                });
            } else if (e.isRightClick() && !current.isEmpty()) {
                current.remove(current.size() - 1);
                templateFile.setNotSave(fullPath(configKey), current);
                msg("&aSuccessfully removed last entry!");
                buildAndOpen();
            }
        });
    }

    protected final void submenuItem(int slot, XMaterial mat, String displayName, Executable action) {
        item(slot, mat, displayName, Arrays.asList("&eClick to browse!"), action);
    }

    protected final void msg(String text) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    protected final Set<String> getKeys(String suffix) {
        String key = suffix.isEmpty() ? fullPath() : fullPath(suffix);
        if (templateFile.getYml().contains(key)) {
            return templateFile.getYml().getConfigurationSection(key).getKeys(false);
        }
        return Collections.emptySet();
    }

}

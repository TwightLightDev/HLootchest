package org.twightlight.hlootchest.setup.main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.browse.TemplateBrowseMenu;
import org.twightlight.hlootchest.setup.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.modules.Settings;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateMenu extends BaseMenu {

    public TemplateMenu(Player p, TYamlWrapper templateFile, String name, SetupSession session) {
        super(p, templateFile, name, "", session);
        open(27, "&7Editing " + name + "...", () -> new TemplateMenu(p, templateFile, name, session));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new TemplateBrowseMenu(p, templateFile));

        submenuItem(11, XMaterial.COMPARATOR, "&bSettings",
                e -> new Settings(p, templateFile, name, ".settings", session));

        submenuItem(13, XMaterial.STONE_BUTTON, "&bButtons",
                e -> new ButtonsMenu(p, templateFile, name, ".buttons", session));

        List<String> locs = templateFile.getYml().contains(name + ".rewards-location")
                ? templateFile.getList(name + ".rewards-location") : Collections.emptyList();
        List<String> lore = locs.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', "&f- " + line))
                .collect(Collectors.toList());
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft-click to add your current location."));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eRight-click to remove the last location."));

        item(15, XMaterial.ARMOR_STAND, ChatColor.YELLOW + "Rewards Location", lore, e -> {
            List<String> current = templateFile.getYml().contains(name + ".rewards-location")
                    ? templateFile.getList(name + ".rewards-location") : Collections.emptyList();
            if (e.isLeftClick()) {
                current.add(Utility.locationToString(p.getLocation()));
                templateFile.setNotSave(name + ".rewards-location", current);
            } else if (e.isRightClick() && !current.isEmpty()) {
                current.remove(current.size() - 1);
                templateFile.setNotSave(name + ".rewards-location", current);
            }
            buildAndOpen();
        });
    }
}

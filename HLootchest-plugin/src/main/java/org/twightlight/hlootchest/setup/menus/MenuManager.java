package org.twightlight.hlootchest.setup.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.setup.functionals.ClickableButtons;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private static final Map<UUID, Map<ItemStack, ClickableButtons>> buttonsList = new HashMap<>();

    public static void setItem(Player p, Inventory inv, ItemStack item, int slot, ClickableButtons actions) {
        inv.setItem(slot, item);
        if (!buttonsList.containsKey(p.getUniqueId())) {
            buttonsList.put(p.getUniqueId(), new HashMap<>());
        }
        buttonsList.get(p.getUniqueId()).put(item, actions);
    }

    public static void removeData(Player p) {
        buttonsList.remove(p.getUniqueId());
    }

    public static Map<UUID, Map<ItemStack, ClickableButtons>> getButtonsList() {
        return buttonsList;
    }
}

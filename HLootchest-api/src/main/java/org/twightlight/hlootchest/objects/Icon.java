package org.twightlight.hlootchest.objects;

import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;

public class Icon implements TIcon {
    private final ItemStack itemStack;
    private final ItemSlot itemSlot;

    public Icon(ItemStack item, ItemSlot slot) {
        itemStack = item;
        itemSlot = slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemSlot getItemSlot() {
        return itemSlot;
    }
}

package org.twightlight.hlootchest.api.interfaces.lootchest;

import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ItemSlot;

public interface TIcon {

    /**
     * Gets the {@link ItemStack} associated with this button.
     *
     * @return The {@link ItemStack} representing the button.
     */
    ItemStack getItemStack();

    /**
     * Gets the {@link ItemSlot} where this button is placed.
     *
     * @return The {@link ItemSlot} of the button.
     */
    ItemSlot getItemSlot();
}

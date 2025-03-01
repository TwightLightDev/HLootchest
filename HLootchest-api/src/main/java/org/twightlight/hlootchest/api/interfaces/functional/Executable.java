package org.twightlight.hlootchest.api.interfaces.functional;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface Executable {
    void execute(InventoryClickEvent e);
}

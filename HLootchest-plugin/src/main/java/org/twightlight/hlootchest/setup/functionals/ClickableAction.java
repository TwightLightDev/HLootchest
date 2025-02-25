package org.twightlight.hlootchest.setup.functionals;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickableAction {
    void execute(InventoryClickEvent e);
}

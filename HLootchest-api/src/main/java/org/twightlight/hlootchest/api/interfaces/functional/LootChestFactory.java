package org.twightlight.hlootchest.api.interfaces.functional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;

@FunctionalInterface
public interface LootChestFactory {
    TBox create(Location location, Player player, ItemStack icon, TConfigManager config, String boxid);
}


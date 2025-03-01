package org.twightlight.hlootchest.api.interfaces.functional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.interfaces.TBox;
import org.twightlight.hlootchest.api.interfaces.TConfigManager;

@FunctionalInterface
public interface LootChestFactory {
    TBox create(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation);
}


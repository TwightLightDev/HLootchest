package org.twightlight.hlootchest.api.supports;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;

@FunctionalInterface
public interface LootChestFactory {
    TBox create(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation);
}


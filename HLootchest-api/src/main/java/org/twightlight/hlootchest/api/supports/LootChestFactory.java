package org.twightlight.hlootchest.api.supports;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.objects.TBox;

@FunctionalInterface
public interface LootChestFactory {
    TBox create(Player player, Location location, ItemStack icon);
}


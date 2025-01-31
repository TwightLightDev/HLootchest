package org.twightlight.hlootchest.api.supports;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;

@FunctionalInterface
public interface LootChestFactory {
    TBox create(Player player, ItemStack icon, TConfigManager config, String boxid);
}


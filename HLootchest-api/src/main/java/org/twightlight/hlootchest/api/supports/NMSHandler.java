package org.twightlight.hlootchest.api.supports;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NMSHandler {
    public static String owner;

    public final Plugin plugin;

    public NMSHandler(Plugin plugin, String versionName) {
        owner = versionName;
        this.plugin = plugin;
    }

    public abstract void registerButtonClick(Player player);
    public abstract void spawnButton(ButtonType typePlayer, Player player, Location location, ItemStack icon);
    public abstract TBox spawnBox(String boxid, Player player, Location location, ItemStack icon);
    public abstract ConcurrentHashMap<Player, List<TButton>> getGlobalButtons();
    public abstract TButton getButtonFromId(int id);
    public abstract ItemStack createItemStack(String material, int amount, short data);
    public abstract void register(String boxid, LootChestFactory function);

}

package org.twightlight.hlootchest.api.supports;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NMSHandler {
    public static String owner;

    public final Plugin plugin;

    public NMSHandler(Plugin plugin, String versionName) {
        owner = versionName;
        this.plugin = plugin;
    }

    public abstract void registerButtonClick(Player player);
    public abstract void spawnButton(Location location, ButtonType typePlayer, Player player, ItemStack icon, String path, TConfigManager config);
    public abstract TBox spawnBox(Location location, String boxid, Player player, ItemStack icon, TConfigManager config, Location initialLocation);
    public abstract void removeButtonsFromPlayer(Player player, ButtonType type);
    public abstract void hideButtonsFromPlayer(Player player, ButtonType type, boolean state);
    public abstract ConcurrentHashMap<Player, List<TButton>> getGlobalButtons();
    public abstract TButton getButtonFromId(int id);
    public abstract TBox getBoxFromPlayer(Player player);
    public abstract ItemStack createItemStack(String material, int amount, short data);
    public abstract void register(String boxid, LootChestFactory function);
    public abstract Map<String, LootChestFactory> getRegistrationData();
    public abstract Location stringToLocation(String locString);
    public abstract void setFakeGameMode(Player p, GameMode gamemode);

}

package org.twightlight.hlootchest.supports.v1_8_R3;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.LootChestFactory;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular;
import org.twightlight.hlootchest.supports.v1_8_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_8_R3.listeners.ClickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class v1_8_R3 extends NMSHandler {

    public static NMSHandler handler;
    private static final Map<String, LootChestFactory> tboxdata = new HashMap<>();

    public v1_8_R3(Plugin pl, String name) {
        super(pl, name);
        handler = this;
    }


    public void registerButtonClick(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection = new ClickEvent(nmsPlayer.playerConnection.networkManager, nmsPlayer);
    }

    public void spawnButton(ButtonType type, Player player, Location location, ItemStack icon) {
        new Button(type, player, location, icon);
    }

    public TBox spawnBox(String boxid, Player player, Location location, ItemStack icon) {
        LootChestFactory factory = tboxdata.get(boxid);
        if (factory == null) {
            player.sendMessage(ChatColor.RED + "Unknow lootchest type!");
            return null;
        }
        return factory.create(player, location, icon);
    }


    public ConcurrentHashMap<Player, List<TButton>> getGlobalButtons() {
        return Button.playerButtonMap;
    }

    public TButton getButtonFromId(int id) {
        return Button.buttonIdMap.get(id);
    }

    public ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            i = new ItemStack(Material.valueOf(material), amount, data);
        } catch (Exception ex) {
            i = new ItemStack(Material.BEDROCK);
        }
        return i;
    }

    public void register(String boxid, LootChestFactory function) {
        if (tboxdata.containsKey(boxid)) {
            throw new IllegalArgumentException("LootChest with id " + boxid + " is already registered!");
        }
        tboxdata.put(boxid, function);
    }

}

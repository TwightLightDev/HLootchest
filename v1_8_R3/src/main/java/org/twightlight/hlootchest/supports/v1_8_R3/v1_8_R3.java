package org.twightlight.hlootchest.supports.v1_8_R3;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.BoxType;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular;
import org.twightlight.hlootchest.supports.v1_8_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_8_R3.listeners.ClickEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class v1_8_R3 extends NMSHandler {

    public static NMSHandler handler;

    public v1_8_R3(Plugin pl, String name) {
        super(pl, name);
        handler = this;
    }

    public void registerButtonClick(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection = new ClickEvent(nmsPlayer.playerConnection.networkManager, nmsPlayer);
    }

    public void spawnButton(Player player, Location location, ButtonType type, ItemStack icon) {
        new Button(player, location, type, icon);
    }

    public void spawnBox(Player player, Location location, BoxType boxType, ItemStack icon) {
        switch (boxType) {
            case REGULAR:
                try {
                    new Regular(player, location, icon, boxType);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
        }
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


}

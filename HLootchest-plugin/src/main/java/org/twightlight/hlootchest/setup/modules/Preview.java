package org.twightlight.hlootchest.setup.modules;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;

import java.util.HashMap;
import java.util.Map;

public class Preview {

    public static Map<Player, Preview> previewList = new HashMap<>();
    private TButton button;
    private Player player;

    public Preview(Location location, ButtonType buttonType, Player player, String path, TYamlWrapper config) {
        if (previewList.containsKey(player)) {
            previewList.get(player).remove();
        }
        previewList.put(player, this);
        this.player = player;
        button = HLootChest.getNms().spawnPreviewButton(location, buttonType, player, path, config);
    }

    public void remove() {
        button.remove();
        previewList.remove(player);
    }
}

package org.twightlight.hlootchest.supports.protocol.v1_19_R3.buttons;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.buttons.AbstractButton;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;

public class Button extends AbstractButton {

    private static final NMSBridge BRIDGE = new NMSBridge();

    public Button(Location location, ButtonType type, Player player, String path, TYamlWrapper config, boolean isPreview) {
        super(location, type, player, path, config, isPreview,
                BRIDGE, Main.api, Main.handler, Main.handler.plugin);
    }

    @Override
    public void remove() {
        if (armorstand != null) {
            armorstand.remove();
            for (org.bukkit.entity.ArmorStand stand : childStands) {
                stand.remove();
            }
        }
        super.remove();
    }
}

package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.main.TemplateMenu;
import org.twightlight.hlootchest.setup.elements.RotationsMenu;
import org.twightlight.libs.xseries.XMaterial;

public class Settings extends BaseMenu {

    public Settings(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session) {
        super(p, templateFile, name, path, session);
        open(27, "&7Settings", () -> new Settings(p, templateFile, name, path, session));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new TemplateMenu(p, templateFile, name, session));
        locationItem(11, "&bPlayer's location", ".player-location");
        locationItem(12, "&bLocation", ".location");
        toggleItem(13, XMaterial.LEVER, "&bClick-to-open", ".click-to-open", false);
        submenuItem(14, XMaterial.COMPASS, "&bRotations",
                e -> new RotationsMenu(p, templateFile, name, path + ".rotations", session,
                        ev -> new Settings(p, templateFile, name, path, session)));
    }
}

package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.elements.ButtonsMenu;
import org.twightlight.hlootchest.setup.elements.ChildrenMenu;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;

public class Button extends EntityEditor {

    public Button(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild) {
        super(p, templateFile, name, path, session, isChild);
        String title = isChild ? "&7Editing child..." : "&7Editing button...";
        open(54, title, () -> new Button(p, templateFile, name, path, session, isChild));
    }

    @Override
    protected Executable selfConstructor() {
        return e -> new Button(p, templateFile, name, path, session, isChild);
    }

    @Override
    protected Executable parentBackAction() {
        if (isChild) return e -> new ChildrenMenu(p, templateFile, name, Utility.getPrevPath(path), session, false);
        return e -> new ButtonsMenu(p, templateFile, name, Utility.getPrevPath(path), session);
    }

    @Override
    protected boolean isRewardType() {
        return false;
    }

    @Override
    protected void populate() {
        if (isChild) {
            populateChildItems();
        } else {
            populateCommonNonChildItems();
            item(53, XMaterial.GLASS, "&bPreview", Arrays.asList("&eClick to preview!"),
                    e -> {
                        new Preview(Utility.stringToLocation(templateFile.getString(fullPath(".location"))),
                                ButtonType.FUNCTIONAL, p, fullPath(), templateFile);
                        p.closeInventory();
                    });
        }
    }

    @Override
    protected int extraNameSlotOffset() { return 0; }
    @Override
    protected int extraIconSlotOffset() { return 0; }
    @Override
    protected int extraChildrenSlotOffset() { return 0; }
    @Override
    protected int extraHoldingSlotOffset() { return 0; }
    @Override
    protected int extraSmallSlotOffset() { return 0; }
}

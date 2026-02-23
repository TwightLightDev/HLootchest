package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.elements.ChildrenMenu;
import org.twightlight.hlootchest.setup.elements.RewardsMenu;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

public class Reward extends EntityEditor {

    public Reward(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, boolean isChild) {
        super(p, templateFile, name, path, session, isChild);
        String title = isChild ? "&7Editing child..." : "&7Editing reward...";
        open(54, title, () -> new Reward(p, templateFile, name, path, session, isChild));
    }

    @Override
    protected Executable selfConstructor() {
        return e -> new Reward(p, templateFile, name, path, session, isChild);
    }

    @Override
    protected Executable parentBackAction() {
        if (isChild) return e -> new ChildrenMenu(p, templateFile, name, Utility.getPrevPath(path), session, true);
        return e -> new RewardsMenu(p, templateFile, name, Utility.getPrevPath(path), session);
    }

    @Override
    protected boolean isRewardType() {
        return true;
    }

    @Override
    protected void populate() {
        if (isChild) {
            populateChildItems();
        } else {
            numericChatItemFloat(11, XMaterial.CHEST, "&bChance", ".chance");
            populateCommonNonChildItems();
            editableListItem(21, XMaterial.COMMAND_BLOCK, "&bRewards", ".rewards");
        }
    }

    @Override
    protected int extraNameSlotOffset() { return 5; }
    @Override
    protected int extraIconSlotOffset() { return 1; }
    @Override
    protected int extraChildrenSlotOffset() { return 1; }
    @Override
    protected int extraHoldingSlotOffset() { return 1; }
    @Override
    protected int extraSmallSlotOffset() { return 1; }
}

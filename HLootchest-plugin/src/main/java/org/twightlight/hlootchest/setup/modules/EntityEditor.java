package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.BaseMenu;
import org.twightlight.hlootchest.setup.ChatPrompt;
import org.twightlight.hlootchest.setup.elements.ChildrenMenu;
import org.twightlight.hlootchest.setup.elements.RequirementsMenu;
import org.twightlight.hlootchest.setup.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;

public abstract class EntityEditor extends BaseMenu {

    protected final boolean isChild;

    protected EntityEditor(Player p, TYamlWrapper templateFile, String name, String path,
                           SetupSession session, boolean isChild) {
        super(p, templateFile, name, path, session);
        this.isChild = isChild;
    }

    protected abstract Executable selfConstructor();
    protected abstract Executable parentBackAction();
    protected abstract boolean isRewardType();

    protected void populateChildItems() {
        parentBackButton();
        locationItem(11, "&bLocation", ".location");

        item(12, XMaterial.ARMOR_STAND, "&bLocation offset",
                Arrays.asList("&aCurrent value: &7" + templateFile.getYml().getString(fullPath(".location-offset"), "null"),
                        "", "&eClick to set to new offset value!"),
                e -> ChatPrompt.promptString(p,
                        Arrays.asList("&aType the value you want: ", "&aLocation-offset support math!", "&aType 'cancel' to cancel!"),
                        this::buildAndOpen, input -> {
                            templateFile.setNotSave(fullPath(".location-offset"), input);
                            templateFile.setNotSave(fullPath(".location"), null);
                            msg("&aSuccessfully set new value to: &e" + input);
                            buildAndOpen();
                        }));

        submenuItem(13, XMaterial.COMPASS, "&bRotations",
                e -> new RotationsMenu(p, templateFile, name, path + ".rotations", session, selfConstructor()));

        submenuItem(14, XMaterial.NAME_TAG, "&bDisplay Name Settings",
                e -> new Name(p, templateFile, name, path + ".name", session, isChild, selfConstructor()));

        submenuItem(15, XMaterial.PLAYER_HEAD, "&bIcons",
                e -> new Icon(p, templateFile, name, path + ".icon", session, isChild, selfConstructor()));

        toggleItem(20, XMaterial.GOLD_NUGGET, "&bSmall", ".small", false);
    }

    protected void populateCommonNonChildItems() {
        parentBackButton();
        locationItem(11, "&bLocation", ".location");

        submenuItem(12, XMaterial.CHEST, "&bClick sound",
                e -> new Sound(p, templateFile, name, path + ".click-sound", session, selfConstructor()));

        submenuItem(13, XMaterial.CHEST, "&bHover sound",
                e -> new Sound(p, templateFile, name, path + ".hover-sound", session, selfConstructor()));

        submenuItem(14, XMaterial.COMPASS, "&bRotations",
                e -> new RotationsMenu(p, templateFile, name, path + ".rotations", session, selfConstructor()));

        numericChatItem(15, XMaterial.CLOCK, "&bDelay", ".delay");

        submenuItem(21, XMaterial.RED_WOOL, "&bSpawn Requirements",
                e -> new RequirementsMenu(p, templateFile, name, path + ".spawn-requirements", session, selfConstructor()));

        submenuItem(22, XMaterial.RED_WOOL, "&bClick Requirements",
                e -> new RequirementsMenu(p, templateFile, name, path + ".click-requirements", session, selfConstructor()));

        item(23, XMaterial.COMPASS, "&bRotate-on-spawn",
                Arrays.asList(
                        "&aEnable: &7" + templateFile.getBoolean(fullPath(".rotate-on-spawn.enable"), false),
                        "&aReverse: &7" + templateFile.getBoolean(fullPath(".rotate-on-spawn.reverse"), false),
                        "&aFinal yaw: &7" + templateFile.getString(fullPath(".rotate-on-spawn.final-yaw"), "null"),
                        "", "&eLeft-click to change 'Enable'!",
                        "&eRight-click to change 'Reverse'!",
                        "&eShift-left-click to change 'Final yaw'!"),
                e -> {
                    if (e.isLeftClick() && e.isShiftClick()) {
                        ChatPrompt.promptNumeric(p, this::buildAndOpen, input -> {
                            templateFile.setNotSave(fullPath(".rotate-on-spawn.final-yaw"), Float.valueOf(input));
                            msg("&aSuccessfully set new final yaw to: &e" + input);
                            buildAndOpen();
                        });
                    } else if (e.isRightClick()) {
                        templateFile.setNotSave(fullPath(".rotate-on-spawn.reverse"),
                                !templateFile.getBoolean(fullPath(".rotate-on-spawn.reverse"), false));
                        msg("&aSuccessfully set new value to: &e" + templateFile.getBoolean(fullPath(".rotate-on-spawn.reverse")));
                        buildAndOpen();
                    } else if (e.isLeftClick()) {
                        templateFile.setNotSave(fullPath(".rotate-on-spawn.enable"),
                                !templateFile.getBoolean(fullPath(".rotate-on-spawn.enable"), false));
                        msg("&aSuccessfully set new value to: &e" + templateFile.getBoolean(fullPath(".rotate-on-spawn.enable")));
                        buildAndOpen();
                    }
                });

        submenuItem(24 + extraNameSlotOffset(), XMaterial.NAME_TAG, "&bDisplay Name Settings",
                e -> new Name(p, templateFile, name, path + ".name", session, isChild, selfConstructor()));

        submenuItem(29 + extraIconSlotOffset(), XMaterial.PLAYER_HEAD, "&bIcons",
                e -> new Icon(p, templateFile, name, path + ".icon", session, isChild, selfConstructor()));

        submenuItem(30 + extraChildrenSlotOffset(), XMaterial.ARMOR_STAND, "&bChildren",
                e -> new ChildrenMenu(p, templateFile, name, path + ".children", session, isRewardType()));

        toggleItem(31 + extraHoldingSlotOffset(), XMaterial.SHEARS, "&bHolding Icon", ".holding-icon", true);
        toggleItem(32 + extraSmallSlotOffset(), XMaterial.GOLD_NUGGET, "&bSmall", ".small", false);

        editableListItem(20, XMaterial.COMMAND_BLOCK, "&bActions", ".actions");
    }

    protected int extraNameSlotOffset() { return 0; }
    protected int extraIconSlotOffset() { return 0; }
    protected int extraChildrenSlotOffset() { return 0; }
    protected int extraHoldingSlotOffset() { return 0; }
    protected int extraSmallSlotOffset() { return 0; }

    private void parentBackButton() {
        backButton(45, parentBackAction());
    }
}


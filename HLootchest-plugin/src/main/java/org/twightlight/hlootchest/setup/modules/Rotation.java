package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.elements.RotationsMenu;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.List;

public class Rotation extends BaseMenu {

    private static final List<String> POSITIONS = Arrays.asList("HEAD", "BODY", "RIGHT_ARM", "LEFT_ARM", "RIGHT_LEG", "LEFT_LEG");
    private final Executable backAction;

    public Rotation(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Editing rotation...", () -> new Rotation(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new RotationsMenu(p, templateFile, name, Utility.getPrevPath(path), session, backAction));

        cycleItem(11, XMaterial.ARMOR_STAND, "&bPosition", ".position", POSITIONS, "null");

        item(12, XMaterial.ARMOR_STAND, "&bRotate Value",
                Arrays.asList("&aCurrent value: &7" + templateFile.getYml().getString(fullPath(".value"), "null"),
                        "", "&eClick to set to new rotate value!"),
                e -> ChatPrompt.prompt(p,
                        Arrays.asList("&aType the value you want: ", "&aThe format should be X, Y, Z", "&aType 'cancel' to cancel!"),
                        input -> {
                            if (!Utility.isXYZFormat(input)) {
                                msg("&cInvalid Format! Cancel the action!");
                                return false;
                            }
                            return true;
                        }, this::buildAndOpen, input -> {
                            templateFile.setNotSave(fullPath(".value"), input);
                            msg("&aSuccessfully set new value to: &e" + input);
                            buildAndOpen();
                        }));
    }
}

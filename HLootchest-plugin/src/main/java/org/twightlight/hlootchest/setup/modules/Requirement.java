package org.twightlight.hlootchest.setup.modules;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.interfaces.functional.Executable;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.sessions.SetupSession;
import org.twightlight.hlootchest.setup.api.BaseMenu;
import org.twightlight.hlootchest.setup.api.ChatPrompt;
import org.twightlight.hlootchest.setup.elements.RequirementsMenu;
import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;

import java.util.Arrays;
import java.util.List;

public class Requirement extends BaseMenu {

    private static final List<String> TYPES = Arrays.asList("has-permission", "string-equals", ">=", ">", "==", "<", "<=", "!=");
    private final Executable backAction;

    public Requirement(Player p, TYamlWrapper templateFile, String name, String path, SetupSession session, Executable backAction) {
        super(p, templateFile, name, path, session);
        this.backAction = backAction;
        open(27, "&7Requirement", () -> new Requirement(p, templateFile, name, path, session, backAction));
    }

    @Override
    protected void populate() {
        backButton(18, e -> new RequirementsMenu(p, templateFile, name, Utility.getPrevPath(path), session, backAction));

        cycleItem(11, XMaterial.COMPARATOR, "&bType", ".type", TYPES, "null");

        String type = templateFile.getYml().getString(fullPath(".type"), "null");

        if ("has-permission".equals(type)) {
            requirementValueItem(12, "value", "&bValue");
        } else if (!"null".equals(type)) {
            requirementValueItem(12, "input", "&bInput");
            requirementValueItem(13, "output", "&bOutput");
        }
    }

    private void requirementValueItem(int slot, String key, String displayName) {
        String type = templateFile.getYml().getString(fullPath(".type"), "null");
        item(slot, XMaterial.FLINT, displayName,
                Arrays.asList("&aCurrent value: &7" + templateFile.getString(fullPath("." + key), "null"),
                        "", "&eClick to set a new " + key + "!"),
                e -> ChatPrompt.prompt(p,
                        Arrays.asList("&aType the value you want: ", "&aType 'cancel' to cancel!"),
                        input -> {
                            if (!"string-equals".equals(type) && !Utility.isNumeric(input)) {
                                msg("&cInvalid Type! Cancel the action!");
                                return false;
                            }
                            return true;
                        }, this::buildAndOpen, input -> {
                            Object finalInput = "string-equals".equals(type) ? input : Float.valueOf(input);
                            templateFile.setNotSave(fullPath("." + key), finalInput);
                            msg("&aSuccessfully set new " + key + " to: &e" + input);
                            buildAndOpen();
                        }));
    }
}

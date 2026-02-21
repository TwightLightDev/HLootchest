package org.twightlight.hlootchest.api.buttons;

import org.twightlight.libs.xseries.XSound;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.objects.ButtonSound;

import java.util.*;

public class ButtonConfigParser {

    public static ButtonConfig parse(TYamlWrapper config, String path, boolean betterCheck) {
        ButtonConfig c = new ButtonConfig();
        c.betterCheckAlgorithm = betterCheck;

        c.actions = config.getList(path + ".actions") != null
                ? config.getList(path + ".actions") : new ArrayList<>();

        if (config.getYml().contains(path + ".click-sound")) {
            c.clickSound = new ButtonSound(
                    XSound.of(config.getString(path + ".click-sound.sound")).get().get(),
                    (float) config.getDouble(path + ".click-sound.yaw"),
                    (float) config.getDouble(path + ".click-sound.pitch")
            );
        }

        if (config.getYml().contains(path + ".hover-sound")) {
            c.hoverSound = new ButtonSound(
                    XSound.of(config.getString(path + ".hover-sound.sound")).get().get(),
                    (float) config.getDouble(path + ".hover-sound.yaw"),
                    (float) config.getDouble(path + ".hover-sound.pitch")
            );
        }

        c.enableName = config.getYml().contains(path + ".name")
                && config.getBoolean(path + ".name.enable");
        c.nameVisibleMode = "always";
        if (config.getYml().contains(path + ".name.visible-mode") && c.enableName) {
            c.nameVisibleMode = config.getString(path + ".name.visible-mode");
            if (c.nameVisibleMode.equals("hover")) {
                c.enableName = false;
            }
        }

        c.dynamicName = config.getYml().contains(path + ".name.dynamic")
                && config.getBoolean(path + ".name.dynamic");

        if (c.dynamicName) {
            c.displayNames = config.getList(path + ".name.display-name") != null
                    ? config.getList(path + ".name.display-name") : new ArrayList<>();
        } else {
            String single = config.getString(path + ".name.display-name");
            c.displayNames = single != null ? Collections.singletonList(single) : Collections.singletonList("");
        }

        c.nameRefreshInterval = config.getYml().contains(path + ".name.refresh-interval")
                ? config.getInt(path + ".name.refresh-interval") : -1;

        c.isSmall = config.getBoolean(path + ".small", false);

        c.holdingIcon = !config.getYml().contains(path + ".holding-icon")
                || config.getBoolean(path + ".holding-icon");

        c.dynamicIcon = config.getYml().contains(path + ".icon.dynamic")
                && config.getBoolean(path + ".icon.dynamic");

        if (c.holdingIcon && !c.dynamicIcon) {
            c.iconMaterial = config.getString(path + ".icon.material");
            c.iconHeadValue = config.getString(path + ".icon.head_value");
            c.iconData = config.getYml().contains(path + ".icon.data")
                    ? config.getInt(path + ".icon.data") : 0;
            c.iconGlowing = config.getBoolean(path + ".icon.glowing", false);
            c.iconSlot = ItemSlot.valueOf(config.getString(path + ".icon.slot", "HEAD"));
        }

        c.iconRefreshInterval = config.getYml().contains(path + ".icon.refresh-interval")
                ? config.getInt(path + ".icon.refresh-interval") : -1;

        if (c.holdingIcon && c.dynamicIcon && config.getYml().contains(path + ".icon.dynamic-icons")) {
            c.dynamicIcons = parseDynamicIcons(config, path + ".icon.dynamic-icons");
        }

        c.moveForward = !config.getYml().contains(path + ".move-forward")
                || config.getBoolean(path + ".move-forward");

        c.rotateOnSpawn = config.getYml().contains(path + ".rotate-on-spawn")
                && config.getBoolean(path + ".rotate-on-spawn.enable");
        if (c.rotateOnSpawn) {
            c.rotateOnSpawnFinalYaw = (float) config.getDouble(path + ".rotate-on-spawn.final-yaw");
            c.rotateReverse = config.getBoolean(path + ".rotate-on-spawn.reverse");
        }

        c.children = new ArrayList<>();
        if (config.getYml().getConfigurationSection(path + ".children") != null) {
            Set<String> childKeys = config.getYml().getConfigurationSection(path + ".children").getKeys(false);
            for (String childName : childKeys) {
                String childPath = path + ".children." + childName;
                c.children.add(parseChild(config, childPath));
            }
        }

        return c;
    }

    private static ChildConfig parseChild(TYamlWrapper config, String path) {
        ChildConfig ch = new ChildConfig();
        ch.path = path;
        ch.locationRaw = config.getString(path + ".location");
        ch.locationOffsetRaw = config.getString(path + ".location-offset");

        ch.enableName = config.getYml().contains(path + ".name")
                && config.getBoolean(path + ".name.enable");
        ch.nameVisibleMode = "always";
        if (config.getYml().contains(path + ".name.visible-mode") && ch.enableName) {
            ch.nameVisibleMode = config.getString(path + ".name.visible-mode");
            if (ch.nameVisibleMode.equals("hover")) {
                ch.enableName = false;
            }
        }

        ch.dynamicName = config.getYml().contains(path + ".name.dynamic")
                && config.getBoolean(path + ".name.dynamic");

        if (ch.dynamicName) {
            ch.displayNames = config.getList(path + ".name.display-name") != null
                    ? config.getList(path + ".name.display-name") : new ArrayList<>();
        } else {
            String single = config.getString(path + ".name.display-name");
            ch.displayNames = single != null ? Collections.singletonList(single) : Collections.singletonList("");
        }

        ch.nameRefreshInterval = config.getYml().contains(path + ".name.refresh-interval")
                ? config.getInt(path + ".name.refresh-interval") : -1;

        ch.isSmall = config.getBoolean(path + ".small", false);

        if (config.getYml().contains(path + ".icon")) {
            ch.hasDynamicIcon = config.getYml().contains(path + ".icon.dynamic")
                    && config.getBoolean(path + ".icon.dynamic");
            ch.iconMaterial = config.getString(path + ".icon.material");
            ch.iconHeadValue = config.getString(path + ".icon.head_value");
            ch.iconData = config.getInt(path + ".icon.data");
            ch.iconGlowing = config.getBoolean(path + ".icon.glowing", false);
            ch.iconSlot = ItemSlot.valueOf(config.getString(path + ".icon.slot", "HEAD"));
            ch.iconRefreshInterval = config.getYml().contains(path + ".icon.refresh-interval")
                    ? config.getInt(path + ".icon.refresh-interval") : -1;
            if (ch.hasDynamicIcon && config.getYml().contains(path + ".icon.dynamic-icons")) {
                ch.dynamicIcons = parseDynamicIcons(config, path + ".icon.dynamic-icons");
            }
        }

        return ch;
    }

    private static List<ButtonConfig.DynamicIconEntry> parseDynamicIcons(TYamlWrapper config, String basePath) {
        List<ButtonConfig.DynamicIconEntry> list = new ArrayList<>();
        Set<String> keys = config.getYml().getConfigurationSection(basePath).getKeys(false);
        for (String key : keys) {
            String p = basePath + "." + key;
            ButtonConfig.DynamicIconEntry e = new ButtonConfig.DynamicIconEntry();
            e.material = config.getString(p + ".material");
            e.headValue = config.getString(p + ".head_value");
            e.data = config.getYml().contains(p + ".data") ? config.getInt(p + ".data") : 0;
            e.glowing = config.getBoolean(p + ".glowing", false);
            e.slot = ItemSlot.valueOf(config.getString(p + ".slot", "HEAD"));
            list.add(e);
        }
        return list;
    }
}


package org.twightlight.hlootchest.api.buttons;

import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.objects.ButtonSound;

import java.util.List;

public class ButtonConfig {
    public List<String> actions;
    public ButtonSound clickSound;
    public ButtonSound hoverSound;
    public boolean enableName;
    public String nameVisibleMode;
    public boolean dynamicName;
    public List<String> displayNames;
    public int nameRefreshInterval;
    public boolean isSmall;
    public boolean holdingIcon;
    public boolean dynamicIcon;
    public int iconRefreshInterval;
    public boolean moveForward;
    public boolean rotateOnSpawn;
    public boolean rotateReverse;
    public float rotateOnSpawnFinalYaw;
    public String iconMaterial;
    public String iconHeadValue;
    public int iconData;
    public boolean iconGlowing;
    public ItemSlot iconSlot;
    public List<DynamicIconEntry> dynamicIcons;
    public List<ChildConfig> children;
    public boolean betterCheckAlgorithm;

    public static class DynamicIconEntry {
        public String material;
        public String headValue;
        public int data;
        public boolean glowing;
        public ItemSlot slot;
    }
}

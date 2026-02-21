package org.twightlight.hlootchest.api.buttons;

import org.twightlight.hlootchest.api.enums.ItemSlot;

import java.util.List;

public class ChildConfig {
    public String path;
    public String locationRaw;
    public String locationOffsetRaw;
    public boolean enableName;
    public String nameVisibleMode;
    public boolean dynamicName;
    public List<String> displayNames;
    public int nameRefreshInterval;
    public boolean isSmall;
    public boolean hasDynamicIcon;
    public String iconMaterial;
    public String iconHeadValue;
    public int iconData;
    public boolean iconGlowing;
    public ItemSlot iconSlot;
    public int iconRefreshInterval;
    public List<ButtonConfig.DynamicIconEntry> dynamicIcons;
}


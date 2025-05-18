package org.twightlight.hlootchest.utils;

import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ProtocolVersion;

public enum DyeColor {
    WHITE(0, 0),
    ORANGE(1, 0),
    MAGENTA(2, 0),
    LIGHT_BLUE(3, 0),
    YELLOW(4, 0),
    LIME(5, 0),
    PINK(6, 0),
    GRAY(7, 0),
    SILVER(8, 0),
    CYAN(9, 0),
    PURPLE(10, 0),
    BLUE(11, 0),
    BROWN(12, 0),
    GREEN(13, 0),
    RED(14, 0),
    BLACK(15, 0);



    final int v1_8_R3;
    final int v1_8_R3_plus;

    DyeColor(int v1_8_R3, int v1_8_R3_plus) {
        this.v1_8_R3 = v1_8_R3;
        this.v1_8_R3_plus = v1_8_R3_plus;
    }

    public int getColorData() {
        if (HLootchest.getNms().getProtocolVersion() == ProtocolVersion.v1_8_R3) {
            return v1_8_R3;
        } else {
            return v1_8_R3_plus;
        }
    }
}

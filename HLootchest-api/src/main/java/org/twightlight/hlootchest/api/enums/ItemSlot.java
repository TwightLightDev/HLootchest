package org.twightlight.hlootchest.api.enums;

public enum ItemSlot {
    HEAD("HEAD"),
    CHESTPLATE("CHESTPLATE"),
    LEGGINGS("LEGGINGS"),
    BOOTS("BOOTS"),
    MAIN_HAND("MAIN_HAND"),
    OFF_HAND("OFF_HAND");

    final String reference;

    ItemSlot(String reference) {
        this.reference = reference;
    }

    public String toString() {
        return reference;
    }
}
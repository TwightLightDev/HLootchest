package org.twightlight.hlootchest.api.enums;

public enum BoxType {

    REGULAR ("regular"),
    OTHERS ("other");

    private String identifier;

    BoxType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

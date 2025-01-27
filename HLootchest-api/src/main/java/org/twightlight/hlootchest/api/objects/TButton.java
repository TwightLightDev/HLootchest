package org.twightlight.hlootchest.api.objects;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.ButtonType;

public interface TButton {
    int getCustomId();
    Player getOwner();
    void remove();
    void moveForward();
    void moveBackward();
    boolean isMoved();
    boolean isClickable();
    void setClickable(boolean bool);
    ButtonType getType();
}

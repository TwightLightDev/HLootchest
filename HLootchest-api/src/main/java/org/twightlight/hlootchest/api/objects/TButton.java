package org.twightlight.hlootchest.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ButtonType;

import java.util.List;

public interface TButton {
    int getCustomId();
    Player getOwner();
    void remove();
    void equipIcon(ItemStack bukkiticon);
    void moveForward();
    void moveBackward();
    boolean isMoved();
    boolean isClickable();
    void setClickable(boolean bool);
    ButtonType getType();
    List<String> getActions();
    boolean isHiding();
    void hide(boolean isHiding);
}

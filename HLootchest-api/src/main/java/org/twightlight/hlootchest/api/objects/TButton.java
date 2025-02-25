package org.twightlight.hlootchest.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
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
    void setIcon(ItemStack icon);
    void hide(boolean isHiding);
    TButtonSound getSound();
    BukkitTask getTask();
    boolean isMoveable();
    void setMoveable(boolean moveable);
    void setType(ButtonType type);
    void setActions(List<String> actions);
    void setSound(TButtonSound sound);
    void setHidingState(boolean hiding);
    ItemStack getIcon();
    void setConfig(TConfigManager config);
    String getPathToButton();
    void setPathToButton(String pathToButton);
    boolean isDynamicName();
    boolean isDynamicIcon();
    String getNameVisibleMode();
    TConfigManager getConfig();
}

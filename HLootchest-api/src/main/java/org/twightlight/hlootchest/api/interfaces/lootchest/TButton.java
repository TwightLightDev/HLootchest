package org.twightlight.hlootchest.api.interfaces.lootchest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.utils.ButtonSound;

import java.util.List;

public interface TButton {
    int getCustomId();
    Player getOwner();
    void remove();
    void equipIcon(TIcon bukkiticon);
    void moveForward();
    void moveBackward();
    boolean isMoved();
    boolean isClickable();
    void setClickable(boolean bool);
    ButtonType getType();
    List<String> getActions();
    boolean isHiding();
    void setIcon(TIcon icon);
    void hide(boolean isHiding);
    ButtonSound getSound();
    BukkitTask getTask();
    boolean isMoveable();
    void setMoveable(boolean moveable);
    void setType(ButtonType type);
    void setActions(List<String> actions);
    void setSound(ButtonSound sound);
    void setHidingState(boolean hiding);
    TIcon getIcon();
    void setConfig(TConfigManager config);
    String getPathToButton();
    void setPathToButton(String pathToButton);
    boolean isDynamicName();
    boolean isDynamicIcon();
    String getNameVisibleMode();
    TConfigManager getConfig();
}

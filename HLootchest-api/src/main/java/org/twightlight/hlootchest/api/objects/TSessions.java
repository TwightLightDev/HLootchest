package org.twightlight.hlootchest.api.objects;

import org.bukkit.entity.Player;

import java.util.Map;

public interface TSessions {
    void close();
    boolean isOpening();
    void setNewBox(TBox box);
}

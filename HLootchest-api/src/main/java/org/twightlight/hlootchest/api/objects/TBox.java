package org.twightlight.hlootchest.api.objects;

import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface TBox {
    void open();
    void remove();
    boolean isClickable();
    void setClickable(boolean bool);
    Player getOwner();
    Location getLoc();
    ItemStack getIcon();
    Location getPlayerInitialLoc();
    void resetPlayerInitialLoc();
    Pig getVehicle();
}

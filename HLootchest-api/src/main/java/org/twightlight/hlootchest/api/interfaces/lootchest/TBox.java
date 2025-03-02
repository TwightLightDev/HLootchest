package org.twightlight.hlootchest.api.interfaces.lootchest;

import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;

import java.util.List;
import java.util.Map;

public interface TBox {
    boolean open();
    void remove();
    void equipIcon(ItemStack bukkiticon);
    boolean isClickable();
    void setClickable(boolean bool);
    Player getOwner();
    Location getLoc();
    ItemStack getIcon();
    Location getPlayerInitialLoc();
    Map<Player, Pig> getVehiclesList();
    void removeVehicle(Player p);
    TBox getInstance();
    boolean isClickToOpen();
    void setOpeningState(Boolean state);
    boolean isOpening();
    Location getPlayerLocation();
    String getBoxId();
    TConfigManager getConfig();
    List<Location> getRewardsLocation();
}

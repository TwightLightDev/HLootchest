package org.twightlight.hlootchest.api.objects;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface TBox {
    void open();
    void remove();
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
    Location getPlayerLocation();
    String getBoxId();
    TConfigManager getConfig();
    List<Location> getRewardsLocation();
    GameMode getGm();
}

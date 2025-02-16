package org.twightlight.hlootchest.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.objects.TBox;

public class LCSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TBox box;

    public LCSpawnEvent(Player player, TBox box) {
        this.box = box;
        this.player = player;
    }


    public Player getPlayer() {
        return this.player;
    }
    public TBox getLootChest() {
        return this.box;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

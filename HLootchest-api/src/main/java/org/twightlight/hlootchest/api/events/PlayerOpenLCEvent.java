package org.twightlight.hlootchest.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.objects.TBox;

public class PlayerOpenLCEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private boolean cancelled = false;
    private TBox box;

    public PlayerOpenLCEvent(Player player, TBox box) {

        this.box = box;
        this.player = player;
    }


    public Player getPlayer() {
        return this.player;
    }

    public TBox getLootChest() {
        return box;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}


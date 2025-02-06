package org.twightlight.hlootchest.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.objects.TBox;

public class PlayerOpenLCEvent extends Event implements Cancellable {

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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}


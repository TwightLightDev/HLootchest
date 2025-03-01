package org.twightlight.hlootchest.api.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.interfaces.TButton;

public class PlayerButtonClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TButton button;
    private boolean cancelled = false;

    public PlayerButtonClickEvent(Player player, TButton button) {
        this.button = button;
        this.player = player;
    }


    public Player getPlayer() {
        return this.player;
    }
    public TButton getButton() {
        return this.button;
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

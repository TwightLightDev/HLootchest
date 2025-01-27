package org.twightlight.hlootchest.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.objects.TButton;

public class PlayerButtonClickEvent extends Event {

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

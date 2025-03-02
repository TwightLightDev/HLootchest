package org.twightlight.hlootchest.api.events.session;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;

public class SessionCloseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TSession session;

    public SessionCloseEvent(Player player, TSession session) {
        this.session = session;
        this.player = player;
    }


    public Player getPlayer() {
        return this.player;
    }
    public TSession getSession() {
        return this.session;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

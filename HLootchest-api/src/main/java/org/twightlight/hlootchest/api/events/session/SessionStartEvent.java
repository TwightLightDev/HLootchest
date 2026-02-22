package org.twightlight.hlootchest.api.events.session;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;

public class SessionStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TSession session;

    public SessionStartEvent(boolean isAsync, Player player, TSession session) {
        super(isAsync);
        this.session = session;
        this.player = player;
    }

    public SessionStartEvent(Player player, TSession session) {

        this(false, player, session);
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

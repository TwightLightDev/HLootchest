package org.twightlight.hlootchest.api.events.lootchest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;

public class ButtonSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final TButton button;

    public ButtonSpawnEvent(Player player, TButton button) {
        this(false, player, button);
    }

    public ButtonSpawnEvent(boolean isAsync, Player player, TButton button) {
        super(isAsync);
        this.button = button;
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public TButton getButton() {
        return this.button;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

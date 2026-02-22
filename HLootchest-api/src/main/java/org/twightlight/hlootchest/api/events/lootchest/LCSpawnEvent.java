package org.twightlight.hlootchest.api.events.lootchest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;

public class LCSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private TBox box;

    public LCSpawnEvent(boolean isAsync, Player player, TBox box) {
        super(isAsync);
        this.box = box;
        this.player = player;
    }

    public LCSpawnEvent(Player player, TBox box) {
        this(false, player, box);
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

package org.twightlight.hlootchest.sessions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.events.session.SessionCloseEvent;
import org.twightlight.hlootchest.api.events.session.SessionStartEvent;
import org.twightlight.hlootchest.api.interfaces.TBox;
import org.twightlight.hlootchest.api.interfaces.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.TSessions;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;


public class SetupSessions extends SessionsManager implements TSessions {

    Player player;

    private MenuHandler<?> inv;

    private TConfigManager conf;

    public SetupSessions(Player p, TConfigManager configManager) {
        player = p;
        SessionsManager.sessions.putIfAbsent(p, this);
        conf = configManager;
        SessionStartEvent event = new SessionStartEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }


    public void close() {
        SessionsManager.sessions.remove(player);
        player.closeInventory();
        SessionCloseEvent event = new SessionCloseEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean isOpening() {
        return false;
    }

    public void setNewBox(TBox box) {
    }

    public void setInvConstructor(MenuHandler<?> inv0) {
        this.inv = inv0;
    }

    public MenuHandler<?> getInvConstructor() {
        return inv;
    }

    public void setConfigManager(TConfigManager config) {
        conf = config;
    }

    public TConfigManager getConfigManager() {
        return conf;
    }
}

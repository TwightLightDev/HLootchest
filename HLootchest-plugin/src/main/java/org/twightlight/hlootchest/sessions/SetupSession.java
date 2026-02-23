package org.twightlight.hlootchest.sessions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.events.session.SessionCloseEvent;
import org.twightlight.hlootchest.api.events.session.SessionStartEvent;
import org.twightlight.hlootchest.api.interfaces.functional.MenuHandler;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.setup.modules.Preview;


public class SetupSession extends SessionsManager implements TSession {

    Player player;

    private MenuHandler<?> inv;

    private TYamlWrapper conf;

    public SetupSession(Player p, TYamlWrapper configManager) {
        player = p;
        SessionsManager.sessions.putIfAbsent(p, this);
        conf = configManager;
        SessionStartEvent event = new SessionStartEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }


    public void close() {
        SessionsManager.sessions.remove(player);
        if (Preview.previewList.containsKey(player)) {
            Preview.previewList.get(player).remove();
        }
        player.closeInventory();
        SessionCloseEvent event = new SessionCloseEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean isOpening() {
        return false;
    }

    public void setBox(TBox box) {
    }

    public void setInvConstructor(MenuHandler<?> inv0) {
        this.inv = inv0;
    }

    public MenuHandler<?> getInvConstructor() {
        return inv;
    }

    public void setConfigManager(TYamlWrapper config) {
        conf = config;
    }

    public TYamlWrapper getConfigManager() {
        return conf;
    }
}

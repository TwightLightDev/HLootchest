package org.twightlight.hlootchest.sessions;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.setup.functionals.MenuHandler;


public class SetupSessions extends SessionsManager implements TSessions {

    Player player;

    private MenuHandler<?> inv;

    private TConfigManager conf;

    public SetupSessions(Player p, TConfigManager configManager) {
        player = p;
        SessionsManager.sessions.putIfAbsent(p, this);
        conf = configManager;
    }


    public void close() {
        SessionsManager.sessions.remove(player);
        player.closeInventory();
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

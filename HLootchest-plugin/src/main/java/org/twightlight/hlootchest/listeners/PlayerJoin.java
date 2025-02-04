package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.twightlight.hlootchest.HLootchest;

import java.util.Map;
import java.util.Set;

public class PlayerJoin implements Listener{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HLootchest.getNms().registerButtonClick(player);

        try {
            HLootchest.getAPI().getDatabaseUtil().getDb().createPlayerData(player);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Map<String, Integer> lchsData = HLootchest.getAPI().getDatabaseUtil().getLootChestData(player);
        Set<String> listLchs = HLootchest.getNms().getRegistrationData().keySet();
        for (String lcType : listLchs) {
            lchsData.putIfAbsent(lcType, 0);
        }
        HLootchest.getAPI().getDatabaseUtil().pullData(player, lchsData);
    }
}

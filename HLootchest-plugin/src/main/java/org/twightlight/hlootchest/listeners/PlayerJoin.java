package org.twightlight.hlootchest.listeners;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.objects.Reward;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Collections;
import java.util.List;
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
            throw new RuntimeException(ex.getMessage());
        }
        Map<String, Integer> lchsData = HLootchest.getAPI().getDatabaseUtil().getDb().getLootChestData(player, "lootchests");
        Map<String, Integer> lchsData1 = HLootchest.getAPI().getDatabaseUtil().getDb().getLootChestData(player, "opened");
        Set<String> listLchs = HLootchest.getNms().getRegistrationData().keySet();
        for (String lcType : listLchs) {
            lchsData.putIfAbsent(lcType, 0);
            lchsData1.putIfAbsent(lcType, 0);
        }
        HLootchest.getAPI().getDatabaseUtil().getDb().pullData(player, lchsData, "lootchests");
        HLootchest.getAPI().getDatabaseUtil().getDb().pullData(player, lchsData1, "opened");

        HLootchest.getAPI().getDatabaseUtil().getDb().addColumnIfNotExists("awaiting_rewards", "TEXT", "NULL");
        Bukkit.getScheduler().runTaskLater(HLootchest.getInstance(), () -> {
            String locS = HLootchest.getAPI().getDatabaseUtil().getDb().getLootChestData(player, "fallback_loc", TypeToken.of(String.class), null);
            try {
                Chunk chunk = player.getLocation().getChunk();
                Utility.clean(chunk, "LootchestVehicle");
                Utility.clean(chunk, "removeOnRestart");

                Location loc = Utility.stringToLocation(locS);

                player.teleport(loc);
                player.setVelocity(new Vector(0, 0, 0));
                HLootchest.getAPI().getDatabaseUtil().getDb().pullData(player, "", "fallback_loc");
            } catch (Exception ignored) {}
        }, 3L);
        Bukkit.getScheduler().runTaskLater(HLootchest.getInstance(), () -> {
            try {
                List<Reward> awaiting_rewards = HLootchest.getAPI().getDatabaseUtil().getDb().getLootChestData(player, "awaiting_rewards", new TypeToken<List<Reward>>() {}, Collections.emptyList());
                if (!awaiting_rewards.isEmpty()) {
                    for (Reward reward : awaiting_rewards) {
                        reward.accept(player);
                    }

                    HLootchest.getAPI().getDatabaseUtil().getDb().pullData(player, null, "awaiting_rewards");
                }

            } catch (Exception ignored) {}
        }, 2L);

    }
}

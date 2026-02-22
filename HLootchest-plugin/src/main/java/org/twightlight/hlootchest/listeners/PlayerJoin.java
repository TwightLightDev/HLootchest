package org.twightlight.hlootchest.listeners;

import com.google.common.reflect.TypeToken;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.objects.Reward;
import org.twightlight.hlootchest.utils.Utility;

import java.util.*;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HLootChest.getNms().registerButtonClick(player);

        TDatabase db = HLootChest.getAPI().getDatabaseUtil().getDatabase();
        if (db == null) return;

        db.createPlayerDataAsync(player).thenCompose(v ->
                db.getLootChestDataAsync(player, "lootchests")
        ).thenCombine(
                db.getLootChestDataAsync(player, "opened"),
                (lchsData, lchsData1) -> {
                    Set<String> listLchs = HLootChest.getNms().getRegistrationData().keySet();
                    for (String lcType : listLchs) {
                        lchsData.putIfAbsent(lcType, 0);
                        lchsData1.putIfAbsent(lcType, 0);
                    }
                    db.updateDataAsync(player, lchsData, "lootchests");
                    db.updateDataAsync(player, lchsData1, "opened");
                    return null;
                }
        ).exceptionally(ex -> {
            HLootChest.getInstance().getLogger().warning("Failed to init player data for " + player.getName() + ": " + ex.getMessage());
            return null;
        });

        db.addColumnIfNotExists("awaiting_rewards", "TEXT", "NULL");

        HLootChest.getScheduler().runTaskLater(player, () -> {
            if (!player.isOnline()) return;
            db.getLootChestDataAsync(player, "fallback_loc", TypeToken.of(String.class), null)
                    .thenAccept(locS -> {
                        if (locS == null || locS.isEmpty()) return;
                        HLootChest.getScheduler().runTask(player, () -> {
                            try {
                                Chunk chunk = player.getLocation().getChunk();
                                Utility.clean(chunk, "LootchestVehicle");
                                Utility.clean(chunk, "removeOnRestart");
                                Location loc = Utility.stringToLocation(locS);
                                if (loc != null) {
                                    player.teleport(loc);
                                    player.setVelocity(new Vector(0, 0, 0));
                                }
                                db.updateDataAsync(player, "", "fallback_loc");
                            } catch (Exception ignored) {}
                        });
                    });
        }, 3L);

        HLootChest.getScheduler().runTaskLater(player, () -> {
            if (!player.isOnline()) return;
            db.getLootChestDataAsync(player, "awaiting_rewards", new TypeToken<List<Reward>>() {}, Collections.emptyList())
                    .thenAccept(awaitingRewards -> {
                        if (awaitingRewards == null || awaitingRewards.isEmpty()) return;
                        HLootChest.getScheduler().runTask(player, () -> {
                            for (Reward reward : awaitingRewards) {
                                reward.accept(player);
                            }
                            db.updateDataAsync(player, null, "awaiting_rewards");
                        });
                    });
        }, 2L);
    }
}

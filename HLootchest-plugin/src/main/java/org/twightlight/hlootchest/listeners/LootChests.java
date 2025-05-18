package org.twightlight.hlootchest.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.player.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.events.player.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.objects.Reward;
import org.twightlight.hlootchest.sessions.LootChestSession;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LootChests implements Listener {

    @EventHandler
    public void onRewardGive(PlayerRewardGiveEvent e) {
        String boxid = e.getLootChest().getBoxId();
        TConfigManager boxConf = HLootchest.getAPI().getConfigUtil().getBoxesConfig(boxid);
        int maxRewards = boxConf.getInt(boxid + ".reward-amount");
        if (boxConf.getYml().getConfigurationSection(boxid + ".rewards-list") == null) {
            return;
        }
        Set<String> prerewards = boxConf.getYml().getConfigurationSection(boxid + ".rewards-list").getKeys(false);
        List<Integer> chances = new ArrayList<>();
        int chance = 0;
        for (String reward : prerewards) {
            if (boxConf.getYml().contains(boxid + ".rewards-list." + reward + ".chance")) {
                chance += boxConf.getInt(boxid + ".rewards-list." + reward + ".chance");
            }
            chances.add(chance);
        }
        Set<String> rewards = Utility.getRandomElements(prerewards, chances, maxRewards);
        List<Reward> awaiting_rewards = new ArrayList<>();
        List<Location> locs = e.getLootChest().getRewardsLocation();
        Random random = new Random();
        for (String reward : rewards) {
            String path = boxid + ".rewards-list" + "." + reward;
            List<String> rewardactions = boxConf.getList(path + ".rewards");

            if (e.getPlayer().isOnline()) {
                Reward reward1 = new Reward(rewardactions);
                reward1.accept(e.getPlayer());
                int randint = random.nextInt(locs.size());
                Location loc = locs.get(randint);
                locs.remove(randint);
                HLootchest.getNms().spawnButton(loc, ButtonType.REWARD, e.getPlayer(), path, boxConf);
            } else {
                awaiting_rewards.add(new Reward(rewardactions));
            }
        }
        if (!e.getPlayer().isOnline()) {
            HLootchest.getAPI().getDatabaseUtil().getDb().pullData(e.getPlayer(), awaiting_rewards, "awaiting_rewards");
        }

    }

    @EventHandler
    public void onDismountEvent(VehicleExitEvent e) {
        Entity exited = e.getExited();
        Player player = null;

        if (exited instanceof Player) {
            player = (Player) exited;
        }

        TSession session = HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(player);

        if (session == null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onLcOpen(PlayerOpenLCEvent e) {
        Player p = e.getPlayer();
        String boxid = e.getLootChest().getBoxId();
        org.twightlight.hlootchest.api.HLootchest.DatabaseUtil api = HLootchest.getAPI().getDatabaseUtil();
        if (api.getDb().getLootChestData(p, "lootchests").get(boxid) > 0) {
            try {
                api.getDb().addLootchest(p, boxid, -1, "lootchests");
                api.getDb().addLootchest(p, boxid, 1, "opened");
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            p.sendMessage(Utility.getMsg(p, "noLootchest"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Pig) {
            Pig vehicle = (Pig) entity;
            if ("LootchestVehicle".equals(vehicle.getCustomName())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onButtonClick(PlayerButtonClickEvent e) {
        TButton button = e.getButton();
        Player p = e.getPlayer();
        TConfigManager configManager = button.getConfig();
        if (!configManager.getYml().contains(button.getPathToButton() + ".click-requirements")) {
            return;
        }
        boolean isSatisfied = Utility.checkConditions(p, configManager, button.getPathToButton() + ".click-requirements");
        if (!isSatisfied) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
            if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof LootChestSession) {
                List<String> allowed_commands = HLootchest.getAPI().getConfigUtil().getMainConfig().getList("allowed-commands.opening");
                for (String command : allowed_commands) {
                    if (e.getMessage().contains(command)) {
                        return;
                    }
                }
                e.setCancelled(true);
                p.sendMessage(Utility.getMsg(p, "noCommand"));
            } else {
                List<String> allowed_commands = HLootchest.getAPI().getConfigUtil().getMainConfig().getList("allowed-commands.setup");
                for (String command : allowed_commands) {
                    if (e.getMessage().contains(command)) {
                        return;
                    }
                }
                e.setCancelled(true);
                p.sendMessage(Utility.getMsg(p, "noCommand"));
            }
        }
    }
    @EventHandler
    public void onBlockInteraction(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
            if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof LootChestSession) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onBlockInteraction(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) != null) {
            if (HLootchest.getAPI().getSessionUtil().getSessionFromPlayer(p) instanceof LootChestSession) {
                e.setCancelled(true);
            }
        }
    }
}

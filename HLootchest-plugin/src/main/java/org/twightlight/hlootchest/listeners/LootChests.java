package org.twightlight.hlootchest.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.API;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LootChests implements Listener {

    @EventHandler
    public void onRewardGive(PlayerRewardGiveEvent e) {
        String boxid = e.getLootChest().getBoxId();
        TConfigManager boxConf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
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
        List<Location> locs = e.getLootChest().getRewardsLocation();
        Random random = new Random();
        for (String reward : rewards) {
            String path = boxid + ".rewards-list" + "." + reward;
            ItemStack buttonicon = Utility.createItem(Material.valueOf(boxConf.getString(path + ".icon.material")), boxConf.getString(path + ".icon.head_value"), boxConf.getInt(path + ".icon.data"), "", new ArrayList<>(), false);

            List<String> rewardactions = boxConf.getList(path + ".rewards");
            for (String action : rewardactions) {
                String[] dataset = action.split(" ", 2);
                if (dataset[0].equals("[player]")) {
                    e.getPlayer().performCommand(dataset[1].replace("{player}", e.getPlayer().getName()));
                } else if (dataset[0].equals("[console]")) {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.getServer().dispatchCommand(console, dataset[1].replace("{player}", e.getPlayer().getName()));
                }
            }
            int randint = random.nextInt(locs.size());
            Location loc = locs.get(randint);
            locs.remove(randint);
            HLootchest.getNms().spawnButton(loc, ButtonType.REWARD, e.getPlayer(), buttonicon, path, boxConf);
        }
    }

    @EventHandler
    public void onDismountEvent(VehicleExitEvent e) {
        Entity entity = e.getVehicle();
        Entity exited = e.getExited();

        if (entity instanceof Pig && exited instanceof Player && !exited.hasPermission("hlootchests.bypass")) {
            Pig vehicle = (Pig) entity;
            Player player = (Player) exited;
            if ("LootchestVehicle".equals(vehicle.getCustomName()) && HLootchest.getNms().getBoxFromPlayer(player) != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLcOpen(PlayerOpenLCEvent e) {
        Player p = e.getPlayer();
        String boxid = e.getLootChest().getBoxId();
        org.twightlight.hlootchest.api.HLootchest.DatabaseUtil api = HLootchest.getAPI().getDatabaseUtil();
        if (api.getLootChestData(p).get(boxid) > 0) {
            try {
                api.addLootChest(p, boxid, -1);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            p.sendMessage(Utility.c(HLootchest.getAPI().getConfigUtil().getMainConfig().getString("Messages.noLootchest")));
            e.setCancelled(true);
        }
    }
}

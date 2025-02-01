package org.twightlight.hlootchest.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RewardListener implements Listener {

    @EventHandler
    public void onRewardGive(PlayerRewardGiveEvent e) {
        String boxid = e.getLootChest().getBoxId();
        TConfigManager boxConf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
        int maxRewards = boxConf.getInt(boxid + ".reward-amount");
        if (boxConf.getYml().getConfigurationSection(boxid + ".rewards-list") == null) {
            return;
        }
        Set<String> prerewards = boxConf.getYml().getConfigurationSection(boxid + ".rewards-list").getKeys(false);
        Set<String> rewards = Utility.getRandomElements(prerewards, maxRewards);
        int i = 0;
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

            HLootchest.getNms().spawnButton(e.getLootChest().getRewardsLocation().get(i), ButtonType.REWARD, e.getPlayer(), buttonicon, path, boxConf);
            i ++;
        }


    }
}

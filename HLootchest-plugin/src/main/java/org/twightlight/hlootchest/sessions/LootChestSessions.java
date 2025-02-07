package org.twightlight.hlootchest.sessions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;
import org.twightlight.hlootchest.utils.Utility;

import java.util.*;

public class LootChestSessions implements TSessions {

    Player player;

    public static final Map<Player, TSessions> sessions = new HashMap<>();

    public LootChestSessions(Player p, String identifier) {
        if (HLootchest.getNms().getBoxFromPlayer(p) == null) {
            player = p;
            sessions.put(p, this);
            TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
            ItemStack icon = Utility.createItem(Material.valueOf(conf.getString(identifier + ".icon.material")),
                    conf.getString(identifier + ".icon.head_value"),
                    conf.getInt(identifier + ".icon.data"), "",
                    new ArrayList<>(),
                    false);
            TConfigManager templateconfig = HLootchest.getAPI().getConfigUtil().getTemplateConfig();
            Location location = HLootchest.getNms().stringToLocation(templateconfig.getString(identifier + ".settings.location"));

            HLootchest.getNms().spawnBox(location, identifier, p, icon, templateconfig, p.getLocation());

            if (templateconfig.getYml().getConfigurationSection(identifier + ".buttons") != null) {
                Set<String> buttons = templateconfig.getYml().getConfigurationSection(identifier + ".buttons").getKeys(false);

                PriorityQueue<ButtonTask> taskQueue = new PriorityQueue<>(Comparator.comparingInt(ButtonTask::getDelay));

                int highestdelay = 0;

                for (String button : buttons) {
                    String path = identifier + ".buttons." + button;
                    int delay = templateconfig.getYml().contains(path + ".delay") ? templateconfig.getInt(path + ".delay") : 0;
                    if (delay > highestdelay) {
                        highestdelay = delay;
                    }
                    taskQueue.add(new ButtonTask(path, delay));
                }

                if (taskQueue.isEmpty()) {
                    return;
                }

                int stop = highestdelay;
                new BukkitRunnable() {
                    int currentTick = 0;
                    @Override
                    public void run() {
                        if (currentTick > stop) {
                            this.cancel();
                            return;
                        }

                        while (!taskQueue.isEmpty() && taskQueue.peek().getDelay() <= currentTick) {
                            ButtonTask task = taskQueue.poll();
                            String path = task.getPath();

                            String iconMaterial = templateconfig.getString(path + ".icon.material");
                            String iconHeadValue = templateconfig.getString(path + ".icon.head_value");
                            int iconData = templateconfig.getInt(path + ".icon.data");
                            String locationString = templateconfig.getString(path + ".location");

                            ItemStack buttonIcon = Utility.createItem(Material.valueOf(iconMaterial), iconHeadValue, iconData, "", new ArrayList<>(), false);
                            Location location = HLootchest.getNms().stringToLocation(locationString);

                            HLootchest.getNms().spawnButton(location, ButtonType.FUNCTIONAL, p, buttonIcon, path, templateconfig);;
                        }

                        currentTick++;
                    }
                }.runTaskTimer(HLootchest.getInstance(), 0L, 1L);
            }
        }
    }

    public void close() {
        TBox box = HLootchest.getNms().getBoxFromPlayer(player);
        box.removeVehicle(player);
        box.getOwner().teleport(box.getPlayerInitialLoc());
        box.remove();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.showPlayer(player);
            }
        }
        HLootchest.getNms().removeButtonsFromPlayer(player, ButtonType.FUNCTIONAL);
        HLootchest.getNms().removeButtonsFromPlayer(player, ButtonType.REWARD);
        player.setGameMode(GameMode.SPECTATOR);
        player.setGameMode(GameMode.SURVIVAL);
    }

    private static class ButtonTask {
        private final String path;
        private final int delay;

        public ButtonTask(String path, int delay) {
            this.path = path;
            this.delay = delay;
        }

        public String getPath() {
            return path;
        }

        public int getDelay() {
            return delay;
        }
    }
}

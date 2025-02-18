package org.twightlight.hlootchest.sessions;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.objects.TSessions;

import java.util.*;

public class LootChestSessions implements TSessions {

    Player player;

    public static final Map<Player, TSessions> sessions = new HashMap<>();

    private TBox box;

    public LootChestSessions(Player p, String identifier) {
        if (HLootchest.getNms().getBoxFromPlayer(p) == null) {
            player = p;
            sessions.put(p, this);
            TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
            ItemStack icon = HLootchest.getNms().createItem(
                    XMaterial.valueOf(conf.getString(identifier + ".icon.material")).parseMaterial(),
                    conf.getString(identifier + ".icon.head_value"),
                    (conf.getYml().contains(identifier + ".icon.data")) ? conf.getInt(identifier + ".icon.data") : 0,
                    "",
                    new ArrayList<>(),
                    false);
            TConfigManager templateconfig = HLootchest.getAPI().getConfigUtil().getTemplateConfig();
            Location location = HLootchest.getNms().stringToLocation(templateconfig.getString(identifier + ".settings.location"));

            box = HLootchest.getNms().spawnBox(location, identifier, p, icon, templateconfig, p.getLocation());

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

                            boolean dynamicIcon = (templateconfig.getYml().contains(path + ".icon.dynamic")) ? templateconfig.getBoolean(path + ".icon.dynamic") : false;
                            String iconMaterial;
                            String iconHeadValue;
                            int iconData;
                            if (!dynamicIcon) {
                                iconMaterial = templateconfig.getString(path + ".icon.material");
                                iconHeadValue = templateconfig.getString(path + ".icon.head_value");
                                iconData = (templateconfig.getYml().contains(path + ".icon.data")) ? templateconfig.getInt(path + ".icon.data") : 0;
                            } else {
                                List<String> iconPaths = new ArrayList<>(templateconfig.getYml().getConfigurationSection(path + ".icon.dynamic-icons").getKeys(false));
                                String thisIconPath = path + ".icon.dynamic-icons." + iconPaths.get(0);
                                iconMaterial = templateconfig.getString(thisIconPath + ".material");
                                iconHeadValue = templateconfig.getString(thisIconPath + ".head_value");
                                iconData = (templateconfig.getYml().contains(thisIconPath + ".data")) ? templateconfig.getInt(thisIconPath + ".data") : 0;
                            }
                            String locationString = templateconfig.getString(path + ".location");

                            ItemStack buttonIcon = HLootchest.getNms().createItem(XMaterial.valueOf(iconMaterial).parseMaterial(), iconHeadValue, iconData, "", new ArrayList<>(), false);
                            Location location = HLootchest.getNms().stringToLocation(locationString);

                            HLootchest.getNms().spawnButton(location, ButtonType.FUNCTIONAL, p, buttonIcon, path, templateconfig);
                        }

                        currentTick++;
                    }
                }.runTaskTimer(HLootchest.getInstance(), 0L, 1L);
            }
        }
    }


    public void close() {
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
        sessions.remove(player);
    }

    public boolean isOpening() {
        return box.isOpening();
    }

    public void setNewBox(TBox box) {
        this.box = box;
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

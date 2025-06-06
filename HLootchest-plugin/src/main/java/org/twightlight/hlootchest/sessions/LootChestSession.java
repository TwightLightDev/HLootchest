package org.twightlight.hlootchest.sessions;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.session.SessionCloseEvent;
import org.twightlight.hlootchest.api.events.session.SessionStartEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.internal.TSession;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.utils.Utility;

import java.util.*;

public class LootChestSession extends SessionsManager implements TSession {

    private Player player;

    private TBox box;

    private GameMode gm;

    private Location initialLocation;

    private Collection<PotionEffect> potionEffects;

    private BukkitTask vehicleTask;

    public LootChestSession(Player p, String identifier) {
        if (HLootchest.getNms().getBoxFromPlayer(p) == null) {
            player = p;
            TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig(identifier);
            ItemStack icon = HLootchest.getNms().createItem(
                    XMaterial.valueOf(conf.getString(identifier + ".icon.material")).get(),
                    conf.getString(identifier + ".icon.head_value"),
                    (conf.getYml().contains(identifier + ".icon.data")) ? conf.getInt(identifier + ".icon.data") : 0,
                    "",
                    new ArrayList<>(),
                    false);
            TConfigManager templateconfig = HLootchest.getAPI().getConfigUtil().getTemplateConfig();
            Location Plocation = Utility.stringToLocation(templateconfig.getString(identifier + ".settings.player-location"));
            Chunk chunk = Plocation.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    potionEffects = p.getActivePotionEffects();
                    potionEffects.forEach(effect ->
                            p.removePotionEffect(effect.getType())
                    );
                    initialLocation = p.getLocation();
                    p.teleport(Plocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.equals(p)) {
                            online.hidePlayer(p);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(HLootchest.getInstance(), () -> {
                        Location location = Utility.stringToLocation(templateconfig.getString(identifier + ".settings.location"));

                        box = HLootchest.getNms().spawnBox(location, identifier, p, icon, templateconfig);
                        gm = p.getGameMode();
                        HLootchest.getNms().getNMSService().setFakeGameMode(p, GameMode.SURVIVAL);

                        if (templateconfig.getYml().getConfigurationSection(identifier + ".buttons") != null) {
                            Set<String> buttons = templateconfig.getYml().getConfigurationSection(identifier + ".buttons").getKeys(false);

                            PriorityQueue<ButtonTask> taskQueue = new PriorityQueue<>(Comparator.comparingInt(ButtonTask::getDelay));
                            int highestDelay = 0;

                            for (String button : buttons) {
                                String path = identifier + ".buttons." + button;
                                if (Utility.checkConditions(p, conf, path + ".spawn-requirements")) {
                                    int delay = templateconfig.getYml().getInt(path + ".delay", 0);
                                    highestDelay = Math.max(highestDelay, delay);
                                    taskQueue.add(new ButtonTask(path, delay));
                                }
                            }

                            if (!taskQueue.isEmpty()) {

                                int finalHighestDelay = highestDelay;
                                new BukkitRunnable() {
                                    int currentTick = 0;
                                    @Override
                                    public void run() {
                                        if (!player.isOnline()) {
                                            this.cancel();
                                        }

                                        while (!taskQueue.isEmpty() && taskQueue.peek().getDelay() <= currentTick) {
                                            ButtonTask task = taskQueue.poll();
                                            String path = task.getPath();

                                            Location location = Utility.stringToLocation(templateconfig.getString(path + ".location"));
                                            HLootchest.getNms().spawnButton(location, ButtonType.FUNCTIONAL, p, path, templateconfig);
                                        }

                                        if (currentTick >= finalHighestDelay || taskQueue.isEmpty()) {
                                            this.cancel();
                                        }

                                        currentTick++;
                                    }
                                }.runTaskTimer(HLootchest.getInstance(), 0L, 1L);
                            }
                        }

                        String initialLoc = Utility.locationToString(initialLocation);
                        HLootchest.getAPI().getDatabaseUtil().getDb().pullData(p, initialLoc, "fallback_loc");
                    }, 1L);
                }
            }.runTaskLater(HLootchest.getInstance(), 1L);

            vehicleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Entity vehicle = box.getVehiclesList().get(player);

                    if (vehicle.getPassenger() != player) {
                        vehicle.eject();
                        vehicle.setPassenger(player);
                    }
                }
            }.runTaskTimer(HLootchest.getInstance(), 20L, 20L);

            SessionStartEvent event = new SessionStartEvent(player, this);
            Bukkit.getPluginManager().callEvent(event);
            SessionsManager.sessions.putIfAbsent(p, this);
        }
    }


    public void close() {
        box.removeVehicle(player);
        box.getOwner().teleport(initialLocation);
        HLootchest.getAPI().getDatabaseUtil().getDb().pullData(box.getOwner(), "", "fallback_loc");
        box.remove();
        HLootchest.getNms().removeButtonsFromPlayer(player, ButtonType.FUNCTIONAL);
        HLootchest.getNms().removeButtonsFromPlayer(player, ButtonType.REWARD);
        player.setGameMode(GameMode.SPECTATOR);
        player.setGameMode(gm);
        SessionsManager.sessions.remove(player);
        potionEffects.forEach(effect ->
                player.addPotionEffect(effect)
        );
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.showPlayer(player);
            }
        }
        vehicleTask.cancel();
        SessionCloseEvent event = new SessionCloseEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean isOpening() {
        return box.isOpening();
    }

    public void setBox(TBox box) {
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

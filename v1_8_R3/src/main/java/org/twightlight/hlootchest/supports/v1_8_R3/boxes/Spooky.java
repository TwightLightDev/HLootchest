package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleData;
import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;
import org.twightlight.hlootchest.utils.LocationFinder;

import java.util.List;
import java.util.Random;

public class Spooky extends BoxManager {
    private final Location location;
    private final Random random = new Random();
    BukkitTask task;

    public Spooky(Location location, Player player, ItemStack icon, TConfigManager config, String boxid) {
        super(location, player, icon, config, boxid);

        this.location = location.clone().add(0, 1.2, 0);

        task = new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                angle += Math.PI / 8;
                if (angle >= 2 * Math.PI) {
                    angle = 0;
                }

                double x1 = 0.6 * Math.cos(angle);
                double z1 = 0.6 * Math.sin(angle);
                double x2 = 0.6 * Math.cos(angle-Math.PI / 8);
                double z2 = 0.6 * Math.sin(angle-Math.PI / 8);
                double y = 1.7;

                Location particleLoc = getBoxReferrence().getLocation().clone().add(x1, y, z1);
                ParticleData data1 = ParticleData.createDustOptions(Color.PURPLE, 2);
                ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc, 3, 0, 0, 0, 0, data1);
                Location particleLoc2 = getBoxReferrence().getLocation().clone().add(x2, y, z2);
                ParticleData data2 = ParticleData.createDustOptions(Color.RED, 4);
                ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc2, 5, 0, 0, 0, 0, data2);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 2L);
    }


    public boolean open() {
        if (!super.open()) {
            return false;
        }

        setOpeningState(true);
        setClickable(false);
        playOpeningAnimation();

        Main.handler.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
        return true;
    }


    private void playOpeningAnimation() {
        Main.nmsUtil.lockAngle(getOwner(), getPlayerLocation(), 112);

        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            double height = 4;
            boolean falling = false;
            Location floor = location.clone();
            float initialYaw = getBoxReferrence().getLocation().getYaw();
            double currentY = floor.getY();
            double velocity = 0;
            int time = 0;

            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 5000) {
                    performFinal();
                    cancel();
                    return;
                }
                if (!getOwner().isOnline()) {
                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
                    Bukkit.getPluginManager().callEvent(event);
                    cancel();
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    double x = (random.nextDouble() - 0.5) * 1.5;
                    double z = (random.nextDouble() - 0.5) * 1.5;
                    ParticleType.of("SMOKE_LARGE").spawn(getOwner(), getBoxReferrence().getLocation().clone().add(x, 0.5, z), 1, 0, 0.1, 0, 0.02);
                }

                if (time % 3 == 0) {
                    int num = 1 + random.nextInt(2);
                    List<Location> locations = LocationFinder.findSafeLocations(getLoc(), num, 3, 8);
                    for (Location location1 : locations) {
                        setTempPumpkin(location1, (5000 - (System.currentTimeMillis() - startTime))/50);
                    }
                }

                if (!falling) {
                    if (velocity == 0) {
                        velocity = Math.sqrt(2 * 0.08 * height);
                    }
                    currentY += velocity;
                    velocity -= 0.08;

                    if (currentY >= floor.getY() + height) {
                        currentY = floor.getY() + height;
                        falling = true;
                        velocity = 0;
                    }
                } else {
                    velocity -= 0.08;
                    currentY += velocity;

                    if (currentY <= floor.getY()) {
                        currentY = floor.getY();
                        falling = false;
                        height /= 1.5;
                        velocity = 0;
                    }
                }

                Location newLoc = floor.clone();
                newLoc.setY(currentY - 1.2);
                Main.nmsUtil.teleport(getOwner(), getBoxReferrence(), newLoc);
                time += 1;
                Main.nmsUtil.spin( getOwner(), getBoxReferrence(), initialYaw + 60*time);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 2L);
    }

    private void performFinal() {
        for (int i = 0; i < 10; i++) {
            Vector direction = new Vector(random.nextDouble() - 0.5, random.nextDouble(), random.nextDouble() - 0.5).normalize().multiply(0.5);
            ParticleType.of("CRIT_MAGIC").spawn(getOwner(), location.clone(), 1, direction.getX(), direction.getY(), direction.getZ(), 0.1);
        }

        location.getWorld().playSound(location, XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 1, 0.5f);
        setOpeningState(false);

        Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_ENDER_DRAGON_GROWL.name(), 20, 5);

        PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
        Bukkit.getPluginManager().callEvent(event);

        remove();
        Main.handler.setFakeGameMode(getOwner(), GameMode.SURVIVAL);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);
        setClickable(true);
        ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), location, 2, 0.5, 0.5, 0.5, 0);

        new Spooky(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId());
    }

    private void setTempPumpkin(Location loc, long duration) {
        if (random.nextInt(2) == 0){
            Main.nmsUtil.setNmsBlock(getOwner(), loc, Material.JACK_O_LANTERN, randomFace());
        }
        else {
            Main.nmsUtil.setNmsBlock(getOwner(), loc, Material.PUMPKIN, randomFace());
        }
        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
            Main.nmsUtil.setNmsBlock(getOwner(), loc, Material.AIR, BlockFace.NORTH);
        }, duration);
    }

    private BlockFace randomFace() {
        int face = 2 + random.nextInt(4);
        switch (face) {
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                return BlockFace.EAST;
        }
    }

    @Override
    public void remove() {
        super.remove();
        task.cancel();
    }
}

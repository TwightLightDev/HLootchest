package org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes;

import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;
import org.twightlight.libs.fastparticles.ParticleData;
import org.twightlight.libs.fastparticles.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.scheduler.ScheduledTask;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;
import org.twightlight.hlootchest.utils.LocationFinder;

import java.util.List;
import java.util.Random;

public class Spooky extends AbstractBox {
    private final Location location;
    private final Random random = new Random();
    ScheduledTask task;

    public Spooky(Location location, Player player, ItemStack icon, TYamlWrapper config, String boxid) {
        super(location, player, icon, config, boxid);

        this.location = location.clone().add(0, 1.2, 0);

        final double[] angle = {0};
        task = Main.api.getScheduler().runTaskTimer(location, () -> {
            if (!player.isOnline()) return;

            angle[0] += Math.PI / 8;
            if (angle[0] >= 2 * Math.PI) angle[0] = 0;

            double x1 = 0.6 * Math.cos(angle[0]);
            double z1 = 0.6 * Math.sin(angle[0]);
            double x2 = 0.6 * Math.cos(angle[0] - Math.PI / 8);
            double z2 = 0.6 * Math.sin(angle[0] - Math.PI / 8);
            double y = 1.7;

            Location particleLoc = getBox().getLocation().clone().add(x1, y, z1);
            ParticleData data1 = ParticleData.createDustOptions(Color.PURPLE, 1);
            ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc, 3, 0, 0, 0, 0, data1);
            Location particleLoc2 = getBox().getLocation().clone().add(x2, y, z2);
            ParticleData data2 = ParticleData.createDustOptions(Color.RED, 1);
            ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc2, 5, 0, 0, 0, 0, data2);
        }, 0L, 2L);
    }

    public boolean open() {
        if (!super.open()) return false;

        setOpeningState(true);
        setClickable(false);
        playOpeningAnimation();

        Main.nmsUtil.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
        return true;
    }

    private void playOpeningAnimation() {
        Main.nmsUtil.lockAngle(getOwner(), getPlayerLocation(), 112);

        final long startTime = System.currentTimeMillis();
        final double[] height = {4};
        final boolean[] falling = {false};
        final Location floor = location.clone();
        final float initialYaw = getBox().getLocation().getYaw();
        final double[] currentY = {floor.getY()};
        final double[] velocity = {0};
        final int[] time = {0};

        Main.api.getScheduler().runTaskTimer(getLoc(), () -> {
            if (System.currentTimeMillis() - startTime > 5000) {
                performFinal();
                return;
            }
            if (!getOwner().isOnline()) {
                PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
                Bukkit.getPluginManager().callEvent(event);
                return;
            }

            for (int i = 0; i < 3; i++) {
                double x = (random.nextDouble() - 0.5) * 1.5;
                double z = (random.nextDouble() - 0.5) * 1.5;
                ParticleType.of("SMOKE_LARGE").spawn(getOwner(), getBox().getLocation().clone().add(x, 0.5, z), 1, 0, 0.1, 0, 0.02);
            }

            if (time[0] % 3 == 0) {
                int i = 1 + random.nextInt(2);
                List<Location> locations = LocationFinder.findSafeLocations(location, i, 3, 8);
                for (Location location1 : locations) {
                    setTempPumpkin(location1, (5000 - (System.currentTimeMillis() - startTime)) / 50);
                }
            }

            if (!falling[0]) {
                if (velocity[0] == 0) velocity[0] = Math.sqrt(2 * 0.08 * height[0]);
                currentY[0] += velocity[0];
                velocity[0] -= 0.08;
                if (currentY[0] >= floor.getY() + height[0]) {
                    currentY[0] = floor.getY() + height[0];
                    falling[0] = true;
                    velocity[0] = 0;
                }
            } else {
                velocity[0] -= 0.08;
                currentY[0] += velocity[0];
                if (currentY[0] <= floor.getY()) {
                    currentY[0] = floor.getY();
                    falling[0] = false;
                    height[0] /= 1.5;
                    velocity[0] = 0;
                }
            }

            Location newLoc = floor.clone();
            newLoc.setY(currentY[0] - 1.2);
            Main.nmsUtil.teleport(getOwner(), getBox(), newLoc);
            time[0] += 1;
            Main.nmsUtil.spin(getOwner(), getBox(), initialYaw + 60 * time[0]);
        }, 0L, 2L);
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
        Main.nmsUtil.setFakeGameMode(getOwner(), GameMode.SURVIVAL);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);
        setClickable(true);
        ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), location, 2, 0.5, 0.5, 0.5, 0);

        new Spooky(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId());
    }

    private void setTempPumpkin(Location location, long duration) {
        int i = random.nextInt(3);
        if (i == 0) {
            Main.nmsUtil.setNmsBlock(getOwner(), location, XMaterial.JACK_O_LANTERN.get(), randomFace());
        } else if (i == 1) {
            Main.nmsUtil.setNmsBlock(getOwner(), location, XMaterial.CARVED_PUMPKIN.get(), randomFace());
        } else {
            Main.nmsUtil.setNmsBlock(getOwner(), location, XMaterial.PUMPKIN.get(), randomFace());
        }
        getOwner().playSound(getOwner().getLocation(), XSound.ENTITY_CHICKEN_EGG.get(), 20, 5);
        Main.api.getScheduler().runTaskLater(location, () -> {
            Main.nmsUtil.setNmsBlock(getOwner(), location, XMaterial.AIR.get(), BlockFace.NORTH);
        }, duration);
    }

    private BlockFace randomFace() {
        int face = 2 + random.nextInt(4);
        switch (face) {
            case 2: return BlockFace.NORTH;
            case 3: return BlockFace.SOUTH;
            case 4: return BlockFace.WEST;
            default: return BlockFace.EAST;
        }
    }

    @Override
    public void remove() {
        super.remove();
        task.cancel();
    }
}

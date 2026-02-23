package org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes;

import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;
import org.twightlight.libs.fastparticles.ParticleData;
import org.twightlight.libs.fastparticles.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.scheduler.ScheduledTask;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Aeternus extends AbstractBox {

    private final List<ArmorStand> clockHands = new ArrayList<>();
    private final List<ArmorStand> floatingNumbers = new ArrayList<>();
    private ScheduledTask rotationTask;
    private ScheduledTask timeWarpTask;
    private final Random random = new Random();
    private final String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};

    public Aeternus(Location location, Player player, ItemStack icon, TYamlWrapper config, String boxid) {
        super(location, player, icon, config, boxid);
        createClockHands(location);
        createClockElements(location);
        startAmbientEffects();
    }

    private void createClockHands(Location center) {
        for (ArmorStand hand : clockHands) {
            Main.nmsUtil.sendDespawnPacket(getOwner(), hand);
        }
        clockHands.clear();

        ItemStack hourHandItem = Main.handler.createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I1ZmFmNGNkODcxMzhjODcxY2M2YTg2NzU4MTdhODk5ODVhM2NiODk3MjFhNGM3NjJmZTY2NmZmNjE4MWMyNCJ9fX0=", 3, "", new ArrayList<>(), true);
        ItemStack minuteHandItem = Main.handler.createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFiNWZkZWZmZmZmYTcwODM4MWU3MGYzNTAzYTI3NTc3MmI0NTI5NmNmOWYxNjI1YTg3ZWRjNmI2MjU0OWVmNiJ9fX0=", 3, "", new ArrayList<>(), true);
        ItemStack secondHandItem = Main.handler.createItem(XMaterial.PLAYER_HEAD.parseMaterial(), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc1N2IxZGY3YzBmYjA1NWFkMDEzMTE5ZDE5NWY3MDMxYTcyM2VkM2NkZGY3NjNjNjM5YTc5ZWNjYjQ4ZmI5YSJ9fX0=", 3, "", new ArrayList<>(), true);

        ArmorStand hourHand = createHand(center, hourHandItem, 0.5f);
        ArmorStand minuteHand = createHand(center, minuteHandItem, 0.8f);
        ArmorStand secondHand = createHand(center, secondHandItem, 1.0f);

        clockHands.add(hourHand);
        clockHands.add(minuteHand);
        clockHands.add(secondHand);
    }

    private ArmorStand createHand(Location center, ItemStack item, float length) {
        Location handLoc = center.clone().add(0, 1.8, 0);
        ArmorStand hand = Main.nmsUtil.createArmorStand(getOwner(), handLoc, "", true, false);
        Main.nmsUtil.sendSpawnPacket(getOwner(), hand);
        Main.nmsUtil.equipIcon(getOwner(), hand, item, ItemSlot.HEAD);
        return hand;
    }

    private void createClockElements(Location center) {
        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians(i * 30);
            double x = Math.cos(angle) * 1.2;
            double z = Math.sin(angle) * 1.2;
            Location numLoc = center.clone().add(x, 1.5, z);

            ArmorStand number = Main.nmsUtil.createArmorStand(getOwner(), numLoc, "&e&l" + numerals[i - 1], true, true);
            Main.nmsUtil.sendSpawnPacket(getOwner(), number);
            floatingNumbers.add(number);
        }

        final double[] angle = {0};
        rotationTask = Main.api.getScheduler().runTaskTimer(getLoc(), () -> {
            if (!getOwner().isOnline()) return;

            updateClockHands(angle[0], angle[0] * 12, angle[0] * 720);

            for (int i = 0; i < floatingNumbers.size(); i++) {
                ArmorStand num = floatingNumbers.get(i);
                double numAngle = Math.toRadians(i * 30 + angle[0] * 5);
                double x = Math.cos(numAngle) * 1.2;
                double z = Math.sin(numAngle) * 1.2;
                Location newLoc = getLoc().clone().add(x, 1.5 + Math.sin(angle[0] * 2) * 0.2, z);
                Main.nmsUtil.teleport(getOwner(), num, newLoc);
            }

            angle[0] += 0.08;

            ParticleData blueData = ParticleData.createDustOptions(Color.fromRGB(0, 150, 255), 1);
            ParticleType.of("REDSTONE").spawn(getOwner(), getLoc().clone().add(0, 1.5, 0), 5, 0.5, 0.5, 0.5, 0, blueData);

            if (random.nextInt(100) < 5) {
                createTimePulse(false);
            }
        }, 0L, 1L);
    }

    private void updateClockHands(double hourAngle, double minuteAngle, double secondAngle) {
        updateHandPosition(clockHands.get(0), hourAngle, 0.5);
        updateHandPosition(clockHands.get(1), minuteAngle, 0.8);
        updateHandPosition(clockHands.get(2), secondAngle, 1.0);
    }

    private void updateHandPosition(ArmorStand hand, double angle, double length) {
        double x = Math.cos(angle) * length;
        double z = Math.sin(angle) * length;
        Location newLoc = getLoc().clone().add(x, 1.8, z);
        float yaw = (float) Math.toDegrees(Math.atan2(-x, -z));
        newLoc.setYaw(yaw);
        Main.nmsUtil.teleport(getOwner(), hand, newLoc);
    }

    private void createTimePulse(boolean isOpening) {
        Location center = getLoc().clone().add(0, 1.5, 0);
        float pitch = isOpening ? 0.5f : 1.0f;
        getOwner().playSound(center, XSound.BLOCK_ANVIL_LAND.get(), 0.7f, pitch);

        final double[] radius = {0.5};
        Color color = isOpening ? Color.fromRGB(255, 200, 0) : Color.fromRGB(100, 200, 255);
        double maxRadius = isOpening ? 5.0 : 3.0;

        Main.api.getScheduler().runTaskTimer(center, () -> {
            if (radius[0] > maxRadius) return;

            ParticleData data = ParticleData.createDustOptions(color, 1);
            for (int i = 0; i < 36; i++) {
                double a = Math.toRadians(i * 10);
                double x = Math.cos(a) * radius[0];
                double z = Math.sin(a) * radius[0];
                Location particleLoc = center.clone().add(x, 0, z);
                ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc, 1, 0, 0, 0, 0, data);
            }
            radius[0] += 0.2;
        }, 0L, 1L);
    }

    private void startAmbientEffects() {
        timeWarpTask = Main.api.getScheduler().runTaskTimer(getLoc(), () -> {
            if (!getOwner().isOnline()) return;
            if (random.nextInt(100) < 5) {
                createTimeWarpPulse();
            }
        }, 0L, 20L);
    }

    private void createTimeWarpPulse() {
        Location center = getLoc().clone().add(0, 1.5, 0);
        getOwner().playSound(center, XSound.BLOCK_ANVIL_USE.get(), 0.5f, 1.8f);

        final double[] radius = {0.5};
        Main.api.getScheduler().runTaskTimer(center, () -> {
            if (radius[0] > 3.0) return;

            for (int i = 0; i < 36; i++) {
                double a = Math.toRadians(i * 10);
                double x = Math.cos(a) * radius[0];
                double z = Math.sin(a) * radius[0];
                Location particleLoc = center.clone().add(x, 0, z);

                if (i % 2 == 0) {
                    ParticleData data = ParticleData.createDustOptions(Color.fromRGB(100, 200, 255), 1);
                    ParticleType.of("REDSTONE").spawn(getOwner(), particleLoc, 1, 0, 0, 0, 0, data);
                } else {
                    ParticleType.of("PORTAL").spawn(getOwner(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
            radius[0] += 0.2;
        }, 0L, 1L);
    }

    @Override
    public boolean open() {
        if (!super.open()) return false;

        setOpeningState(true);
        setClickable(false);

        startTimeWarpSequence();
        Main.nmsUtil.lockAngle(getOwner(), getPlayerLocation(), 120);

        Main.nmsUtil.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);

        return true;
    }

    private void startTimeWarpSequence() {
        long current_time = getOwner().getPlayerTime();

        if (rotationTask != null) rotationTask.cancel();
        if (timeWarpTask != null) timeWarpTask.cancel();

        getOwner().playSound(getLoc(), XSound.BLOCK_PORTAL_TRAVEL.get(), 1.0f, 0.5f);
        createTimePulse(true);

        final double[] speed = {1.0};
        final int[] phase = {0};
        final int[] point = {0};

        Main.api.getScheduler().runTaskTimer(getLoc(), () -> {
            if (!getOwner().isOnline()) return;

            if (phase[0] == 0) {
                speed[0] *= 0.95;
                updateClockHands(
                        (speed[0] + Math.sin(Math.toRadians(random.nextInt(360))) * 0.1) * 0.5,
                        (speed[0] + Math.sin(Math.toRadians(random.nextInt(360))) * 0.1) * 6,
                        (speed[0] + Math.sin(Math.toRadians(random.nextInt(360))) * 0.1) * 30
                );

                ParticleData slowData = ParticleData.createDustOptions(Color.fromRGB(200, 200, 255), 1);
                for (ArmorStand hand : clockHands) {
                    ParticleType.of("REDSTONE").spawn(getOwner(), hand.getLocation(), 2, 0, 0, 0, 0, slowData);
                }

                getOwner().setPlayerTime(current_time + 1200L * point[0], false);
                point[0]++;

                if (speed[0] < 0.1) {
                    phase[0] = 1;
                    getOwner().playSound(getLoc(), XSound.BLOCK_PORTAL_TRIGGER.get(), 1.0f, 0.7f);
                }
            } else if (phase[0] == 1) {
                ParticleType.of("CRIT_MAGIC").spawn(getOwner(), getLoc().clone().add(0, 1.5, 0), 20, 1.5, 1.5, 1.5, 0.1);

                Main.api.getScheduler().runTaskLater(getLoc(), () -> {
                    phase[0] = 2;
                    getOwner().playSound(getLoc(), XSound.BLOCK_PORTAL_TRIGGER.get(), 1.0f, 1.5f);
                }, 40L);
            } else if (phase[0] == 2) {
                speed[0] *= 1.1;
                updateClockHands(-speed[0] * 0.5, -speed[0] * 6, -speed[0] * 30);

                ParticleData reverseData = ParticleData.createDustOptions(Color.fromRGB(255, 150, 150), 1);
                for (ArmorStand hand : clockHands) {
                    ParticleType.of("REDSTONE").spawn(getOwner(), hand.getLocation(), 2, 0, 0, 0, 0, reverseData);
                }

                long current_time_2 = getOwner().getPlayerTime();
                getOwner().setPlayerTime(current_time_2 - point[0] * 1200L, false);
                point[0]--;

                if (speed[0] > 5.0) {
                    performTimeExplosion();
                    getOwner().setPlayerTime(current_time, true);
                    return;
                }
            }
        }, 0L, 1L);
    }

    private void performTimeExplosion() {
        ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), getLoc().clone().add(0, 1.5, 0), 3);
        ParticleType.of("FIREWORKS_SPARK").spawn(getOwner(), getLoc().clone().add(0, 1.5, 0), 50, 2, 2, 2, 0.3);

        getOwner().playSound(getLoc(), XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 1.0f, 0.8f);

        PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
        Bukkit.getPluginManager().callEvent(event);

        setOpeningState(false);
        remove();
        Main.nmsUtil.setFakeGameMode(getOwner(), GameMode.SURVIVAL);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);
        setClickable(true);

        new Aeternus(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId());
    }

    @Override
    public void remove() {
        super.remove();
        for (ArmorStand hand : clockHands) {
            Main.nmsUtil.sendDespawnPacket(getOwner(), hand);
        }
        for (ArmorStand number : floatingNumbers) {
            Main.nmsUtil.sendDespawnPacket(getOwner(), number);
        }
        if (rotationTask != null) rotationTask.cancel();
        if (timeWarpTask != null) timeWarpTask.cancel();
    }
}

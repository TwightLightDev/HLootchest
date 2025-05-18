package org.twightlight.hlootchest.supports.v1_19_R3.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.Collections;
import java.util.Random;

public class Mystic extends BoxManager {

    private ArmorStand floatingOrb1;
    private ArmorStand floatingOrb2;
    private BukkitTask task;

    public Mystic(Location location, Player player, ItemStack icon, TConfigManager config, String boxid) {
        super(location, player, icon, config, boxid);

        Location orbLocation1 = location.clone().add(1, 1.0, 0);
        Location orbLocation2 = location.clone().add(-1, 1.0, 0);

        floatingOrb1 = Main.nmsUtil.createArmorStand(getOwner() ,orbLocation1, "", false);
        floatingOrb2 = Main.nmsUtil.createArmorStand(getOwner() ,orbLocation2, "", false);

        Main.rotate((floatingOrb2), config, boxid+".settings.decoration.2");
        Main.rotate((floatingOrb1), config, boxid+".settings.decoration.1");

        Main.nmsUtil.sendSpawnPacket(getOwner(), floatingOrb1);
        Main.nmsUtil.sendSpawnPacket(getOwner(), floatingOrb2);


        ItemStack orbItem = Main.handler.createItem(XMaterial.IRON_SWORD.parseMaterial(),
                "",
                0,
                "",
                Collections.emptyList(),
                false);

        Main.nmsUtil.equipIcon(getOwner(), floatingOrb1, orbItem, ItemSlot.MAIN_HAND);
        Main.nmsUtil.equipIcon(getOwner(), floatingOrb2, orbItem, ItemSlot.MAIN_HAND);


        task = new BukkitRunnable() {
            double angle = 0;
            double time = 0;

            @Override
            public void run() {

                angle += 5;
                double radians = Math.toRadians(angle);

                double x1 = Math.cos(radians) * 1;
                double z1 = Math.sin(radians) * 1;

                double x2 = Math.cos(-radians) * 1;
                double z2 = Math.sin(-radians) * 1;

                double yOffset = Math.sin(time) * 0.3;


                Location loc1 = location.clone().add(x1, 1.5 + yOffset, z1);
                Location loc2 = location.clone().add(x2, 1.5 + yOffset, z2);

                Main.nmsUtil.teleport(getOwner(), floatingOrb1, loc1);
                Main.nmsUtil.teleport(getOwner(), floatingOrb2, loc2);

                ParticleType.of("ENCHANTMENT_TABLE").spawn(getOwner(), floatingOrb1.getLocation(), 3, 0.2, 0.2, 0.2, 0.1);
                ParticleType.of("CRIT_MAGIC").spawn(getOwner(), floatingOrb1.getLocation(), 3, 0.2, 0.2, 0.2, 0.1);

                ParticleType.of("ENCHANTMENT_TABLE").spawn(getOwner(), floatingOrb2.getLocation(), 3, 0.2, 0.2, 0.2, 0.1);
                ParticleType.of("CRIT_MAGIC").spawn(getOwner(), floatingOrb2.getLocation(), 3, 0.2, 0.2, 0.2, 0.1);

                time += 0.1;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    @Override
    public boolean open() {
        if (!super.open()) {
            return false;
        }

        setOpeningState(true);
        setClickable(false);
        createMysticAnimation();

        Main.handler.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);

        return true;
    }

    private void createMysticAnimation() {
        Main.nmsUtil.lockAngle(getOwner(), getPlayerLocation(), 70);
        Location headLoc = getLoc().clone().add(0, 1.2, 0);
        summonGlyph(headLoc, 60);
        summonRay(headLoc, 60);
        summonLightning(headLoc, 60);
        summonCircle(headLoc.clone().add(0, 0.2, 0), 60, 36 , 1, ParticleType.of("PORTAL"));
        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            double scale = 1.0;
            boolean shrinking = false;
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 3000) {
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

                scale += shrinking ? -0.1 : 0.1;
                if (scale >= 1.5) shrinking = true;
                if (scale <= 0.8) shrinking = false;

                EulerAngle newPose = new EulerAngle(Math.toRadians(0), Math.toRadians(scale * 10.0F), Math.toRadians(0));
                getBox().setHeadPose(newPose);

                ParticleType.of("SPELL_WITCH").spawn(getOwner(), getLoc().clone().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.05);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    private void performFinal() {
        ParticleType.of("EXPLOSION_LARGE").spawn(getOwner(), getLoc().clone().add(0, 1.2, 0), 5, 0.5, 0.5, 0.5, 0.1);
        ParticleType.of("FIREWORKS_SPARK").spawn(getOwner(), getLoc().clone().add(0, 1.2, 0), 10, 1, 1, 1, 0.2);
        setOpeningState(false);

        Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_ENDER_DRAGON_GROWL.name(), 20, 5);

        PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
        Bukkit.getPluginManager().callEvent(event);

        remove();
        Main.handler.setFakeGameMode(getOwner(), GameMode.SURVIVAL);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);
        setClickable(true);

        new Mystic(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId());
    }

    private void summonGlyph(Location location, int durationTicks) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || !getOwner().isOnline()) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    double x = (Math.random() - 0.5) * 2;
                    double z = (Math.random() - 0.5) * 2;
                    Location loc = location.clone().add(x, 1 + (ticks * 0.05), z);

                    ParticleType.of("SPELL_WITCH").spawn(getOwner(), (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 3, 0, 0, 0, 0);
                    ParticleType.of("VILLAGER_HAPPY").spawn(getOwner(), (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 3, 0, 0, 0, 0);

                }
                ticks += 3;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 3L);
    }

    public void summonRay(Location location, int durationTicks) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || !getOwner().isOnline()) {
                    cancel();
                    return;
                }

                for (double y = 0; y <= 10; y += 0.5) {
                    ParticleType.of("ENCHANTMENT_TABLE")
                            .spawn(getOwner(), location.clone().add(0, y, 0), 3, 0.2, 0.2, 0.2, 0.1);
                    getOwner().playSound(getOwner().getLocation(), XSound.BLOCK_BEACON_ACTIVATE.get(), 10, 3);
                }

                ticks += 4;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 4L);
    }

    public void summonLightning(Location center, int durationTicks) {
        Random random = new Random();
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= durationTicks || !getOwner().isOnline()) {
                    cancel();
                    return;
                }
                int num = 1 + random.nextInt(2);
                for (int i = 0; i < num; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 16;
                    double offsetZ = (random.nextDouble() - 0.5) * 16;
                    Location strikeLoc = center.clone().add(offsetX, 0, offsetZ);
                    if (Main.hasPacketService()) {
                        Main.getPacketService().spawnLightning(getOwner(), strikeLoc);

                    }

                    getOwner().playSound(getPlayerLocation(), XSound.ENTITY_LIGHTNING_BOLT_THUNDER.parseSound(), 10, 0.8f + (random.nextFloat()) * 0.4f);

                }
                ticks += 15;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 15L);
    }

    private void summonCircle(Location center, int durationTicks, int points, double radius, ParticleType particle) {
        new BukkitRunnable() {
            double angle = 0;
            int duration = 0;

            @Override
            public void run() {
                if (duration >= durationTicks || !getOwner().isOnline()) {
                    cancel();
                    return;
                }

                for (int i = 0; i < points; i++) {
                    double radians = Math.toRadians((double) i / points * 360 + angle);
                    double x = Math.cos(radians) * radius;
                    double z = Math.sin(radians) * radius;
                    Location particleLoc = center.clone().add(x, 0, z);

                    particle.spawn(getOwner(), particleLoc, 1, 0, 0, 0, 0);
                }
                duration += 2;
                angle += 5;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 2L);
    }

    @Override
    public void remove() {
        super.remove();
        Main.nmsUtil.sendDespawnPacket(getOwner(), floatingOrb1);
        Main.nmsUtil.sendDespawnPacket(getOwner(), floatingOrb2);
        task.cancel();
    }
}


package org.twightlight.hlootchest.supports.v1_18_R2.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.supports.v1_18_R2.Main;

import java.util.Collections;
import java.util.Random;

public class Mystic extends BoxManager {

    private ArmorStand floatingOrb1;
    private ArmorStand floatingOrb2;
    private BukkitTask task;

    public Mystic(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        super(location, player, icon, config, boxid, initialLocation);

        Location orbLocation1 = location.clone().add(1, 1.0, 0);
        Location orbLocation2 = location.clone().add(-1, 1.0, 0);

        floatingOrb1 = Main.nmsUtil.createArmorStand(getOwner() ,orbLocation1, "", false);
        floatingOrb2 = Main.nmsUtil.createArmorStand(getOwner() ,orbLocation2, "", false);

        Main.rotate(((CraftArmorStand) floatingOrb2).getHandle(), config, boxid+".settings.decoration.2");
        Main.rotate(((CraftArmorStand) floatingOrb1).getHandle(), config, boxid+".settings.decoration.1");

        Main.nmsUtil.sendSpawnPacket(getOwner(), floatingOrb1);
        Main.nmsUtil.sendSpawnPacket(getOwner(), floatingOrb2);


        ItemStack orbItem = Main.handler.createItem(XMaterial.IRON_SWORD.parseMaterial(),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNkYjM1M2QzYzAyODg3MWMwZmFlMGNkMGU3NmE2NWIzOTU3OTU5YmNhMThkMDY1YjY3ZjkxMTdiYWQxOWQ4NCJ9fX0=",
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

                DataWatcher dataWatcher = getBox().ai();

                Vector3f pose = new Vector3f(0, (float) scale*10, 0);
                dataWatcher.b(new DataWatcherObject<>(16, DataWatcherRegistry.k), pose);
                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(getBox().ae(), dataWatcher, true);
                ((CraftPlayer) getOwner()).getHandle().b.a(packet);

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

        new Mystic(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId(), getPlayerInitialLoc());
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

                    EntityLightning lightning = new EntityLightning(EntityTypes.U, ((CraftWorld) strikeLoc.getWorld()).getHandle());
                    Entity lightningEntity = lightning.getBukkitEntity();
                    Main.nmsUtil.sendSpawnPacket(getOwner(), lightningEntity);

                    getOwner().playSound(getPlayerLocation(), XSound.ENTITY_LIGHTNING_BOLT_THUNDER.parseSound(), 10, 0.8f + (random.nextFloat()) * 0.4f);

                    Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                        Main.nmsUtil.sendDespawnPacket(getOwner(), lightningEntity);
                    }, 5L);
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


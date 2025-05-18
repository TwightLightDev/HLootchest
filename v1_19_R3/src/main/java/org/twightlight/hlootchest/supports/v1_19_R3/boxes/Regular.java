package org.twightlight.hlootchest.supports.v1_19_R3.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.player.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.v1_19_R3.utilities.Animations;

public class Regular extends BoxManager {
    private final ArmorStand sword;
    public Regular(Location location, Player player, org.bukkit.inventory.ItemStack icon, TConfigManager config, String boxid) {
        super(location, player, icon, config, boxid);
        final Location loc = Main.handler.stringToLocation(config.getString(boxid + ".settings.decoration.location"));

        sword = createArmorStand(loc, "", false);

        Main.rotate(sword, config, boxid + ".settings.decoration");
        sendSpawnPacket(getOwner(), sword);
        ItemStack icon1 = new ItemStack(XMaterial.DIAMOND_SWORD.parseMaterial());
        sword.getEquipment().setItem(EquipmentSlot.HAND, icon1);
        new BukkitRunnable() {
            final double startY = getBox().getLocation().getY();
            final double targetY = getBox().getLocation().getY() - 4.6;
            final int totalTicks = (int) ((startY - targetY) / 0.2);
            int currentTick = 0;
            final double swordOffset = sword.getLocation().getY() - getBox().getLocation().getY();

            @Override
            public void run() {
                if (currentTick >= totalTicks) {
                    cancel();
                    getOwner().playSound(getOwner().getLocation(), XSound.BLOCK_ANVIL_LAND.get(), 10, 5);
                    return;
                }

                double progress = (double) currentTick / totalTicks;
                double newY_box = startY - ((startY - targetY) * progress);
                double newY_sword = newY_box + swordOffset;

                Location swnewLoc = sword.getLocation().clone();
                swnewLoc.setY(newY_sword);
                sword.teleport(swnewLoc);
                Location boxnewLoc = getBox().getLocation().clone();
                boxnewLoc.setY(newY_box);
                getBox().teleport(boxnewLoc);

                currentTick++;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    public boolean open() {
        if (!super.open()) return false;
        setOpeningState(true);
        setClickable(false);
        moveUp();

        if (Main.hasPacketService()) {
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(false)
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                    .withFade(Color.GREEN, Color.BLUE)
                    .withTrail()
                    .build();
            Main.getPacketService().spawnFirework(getOwner(), getPlayerLocation().add(0, 1, 0), effect);
        }
        Main.handler.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > 3500L)
                    return;
                if (getOwner().getLocation().getYaw() != Regular.this.getPlayerLocation().getYaw() || getOwner().getLocation().getPitch() != Regular.this.getPlayerLocation().getPitch()) {
                    getOwner().teleport(getPlayerLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 2L);
        new BukkitRunnable() {
            private long startTime = System.currentTimeMillis();
            private int time = 0;

            @Override
            public void run() {
                Player player = Regular.this.getOwner();

                if (System.currentTimeMillis() - this.startTime > 3000L) {
                    Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                        setOpeningState(false);
                    }, 2L);
                    Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_CAT_AMBIENT.name(), 20, 5);

                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
                    Bukkit.getPluginManager().callEvent(event);

                    remove();

                    Main.handler.setFakeGameMode(getOwner(), GameMode.SURVIVAL);

                    Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);

                    setClickable(true);
                    ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), getLoc().clone().add(0, -3.2, 0), 2, 0.5, 0.5, 0.5, 0);

                    Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_GENERIC_EXPLODE.name(), 20, 5);

                    new Regular(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId());
                    cancel();
                    return;
                }

                ArmorStand box = getBox();
                if (box == null) {
                    cancel();
                    return;
                }

                Main.handler.playSound(player, player.getLocation(), XSound.ENTITY_CHICKEN_EGG.name(), 20.0F, 5.0F);
                ParticleType.of("CLOUD").spawn(Regular.this.getOwner(), Regular.this.getLoc().clone().add(0.0D, -2.8D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);                time++;

                float x, z;
                switch (time % 4) {
                    case 0: x = 6.0F; z = 6.0F; break;
                    case 1: x = -6.0F; z = 6.0F; break;
                    case 2: x = -6.0F; z = -6.0F; break;
                    default: x = 6.0F; z = -6.0F; break;
                }

                EulerAngle newPose = new EulerAngle(Math.toRadians(x), Math.toRadians(time * 18.0F), Math.toRadians(z));
                box.setHeadPose(newPose);
            }
        }.runTaskTimer(Main.handler.plugin, 20L, 1L);
        return true;
    }

    public void remove() {
        super.remove();
        sword.remove();
    }

    private void moveUp() {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            public void run() {
                if (System.currentTimeMillis() - this.startTime > 100L)
                    cancel();
                Animations.moveUp(getOwner(), sword, 0.06F);
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 1L);
    }
}
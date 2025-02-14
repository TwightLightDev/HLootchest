package org.twightlight.hlootchest.supports.v1_19_R3.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.v1_19_R3.utilities.Animations;

public class Regular extends BoxManager {
    private final ArmorStand sword;
    public Regular(Location location, Player player, org.bukkit.inventory.ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        super(location, player, icon, config, boxid, initialLocation);
        final Location loc = Main.handler.stringToLocation(config.getString(boxid + ".settings.decoration.location"));

        this.sword = createArmorStand(loc, "", false);

        Main.rotate(this.sword, config, boxid + ".settings.decoration");
        sendSpawnPacket(getOwner(), this.sword);
        ItemStack icon1 = new ItemStack(XMaterial.DIAMOND_SWORD.parseMaterial());
        sword.getEquipment().setItem(EquipmentSlot.HAND, icon1);
        (new BukkitRunnable() {
            public void run() {
                if (Regular.this.getBox().getLocation().getY() < loc.clone().getY() - 5.4D)
                    cancel();
                Animations.moveUp(Regular.this.getOwner(), Regular.this.sword, -0.2F);
                Animations.moveUp(Regular.this.getOwner(), Regular.this.getBox(), -0.2F);
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    public boolean open() {
        if (!super.open()) return false;
        setOpeningState(true);
        setClickable(false);
        moveUp();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).with(FireworkEffect.Type.BURST).withColor(new Color[] { Color.RED, Color.ORANGE, Color.YELLOW }).withFade(new Color[] { Color.GREEN, Color.BLUE }).withTrail().build();
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
                Location loc = Regular.this.getLoc();

                if (System.currentTimeMillis() - startTime > 3000L) {
                    Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                        setOpeningState(false);
                    }, 2L);
                    Main.handler.playSound(player, player.getLocation(), XSound.ENTITY_CAT_AMBIENT.name(), 20.0F, 5.0F);
                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(player, Regular.this.getInstance());
                    Bukkit.getPluginManager().callEvent(event);
                    Regular.this.remove();
                    Main.handler.setFakeGameMode(player, GameMode.SURVIVAL);
                    Main.handler.hideButtonsFromPlayer(player, ButtonType.FUNCTIONAL, false);
                    Regular.this.setClickable(true);
                    ParticleType.of("EXPLOSION_HUGE").spawn(Regular.this.getOwner(), Regular.this.getLoc().clone().add(0.0D, -3.2D, 0.0D), 2, 0.5D, 0.5D, 0.5D, 0.0D);                    Main.handler.playSound(player, player.getLocation(), XSound.ENTITY_GENERIC_EXPLODE.name(), 20.0F, 5.0F);
                    new Regular(loc, player, Regular.this.getIcon(), Regular.this.getConfig(), Regular.this.getBoxId(), Regular.this.getPlayerInitialLoc());
                    cancel();
                    return;
                }

                ArmorStand box = Regular.this.getBox();
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
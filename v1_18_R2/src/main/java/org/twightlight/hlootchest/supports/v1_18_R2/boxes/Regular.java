package org.twightlight.hlootchest.supports.v1_18_R2.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.datafixers.util.Pair;
import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_18_R2.Main;
import org.twightlight.hlootchest.supports.v1_18_R2.utilities.Animations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Regular extends BoxManager {
    private EntityArmorStand sword;

    public Regular(Location location, Player player, org.bukkit.inventory.ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        super(location, player, icon, config, boxid, initialLocation);
        final Location loc = Main.handler.stringToLocation(config.getString(boxid + ".settings.decoration.location"));
        this.sword = createArmorStand(loc, "", false);
        Main.rotate(this.sword, config, boxid + ".settings.decoration");
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.sword.ae(), this.sword.ai(), true);
        (((CraftPlayer)getOwner()).getHandle()).b.a((Packet)metadataPacket);
        sendSpawnPacket(getOwner(), this.sword);
        ItemStack icon1 = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(XMaterial.DIAMOND_SWORD.parseMaterial()));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.sword.ae(), Collections.singletonList(new Pair(EnumItemSlot.a, icon1)));
        (((CraftPlayer)getOwner()).getHandle()).b.a((Packet)packet);
        (new BukkitRunnable() {
            public void run() {
                if (Regular.this.getBox().de() < loc.clone().getY() - 5.2D)
                    cancel();
                Animations.MoveUp(Regular.this.getOwner(), Regular.this.sword, -0.2F);
                Animations.MoveUp(Regular.this.getOwner(), Regular.this.getBox(), -0.2F);
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    public boolean open() {
        if (!super.open())
            return false;
        setOpeningState(true);
        setClickable(false);
        moveUp();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).with(FireworkEffect.Type.BURST).withColor(new Color[] { Color.RED, Color.ORANGE, Color.YELLOW }).withFade(new Color[] { Color.GREEN, Color.BLUE }).withTrail().build();
        Animations.spawnFireWork(getOwner(), getOwner().getLocation(), effect);
        Main.handler.setFakeGameMode(getOwner(), GameMode.SPECTATOR);
        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            public void run() {
                if (System.currentTimeMillis() - this.startTime > 3500L)
                    return;
                EntityPlayer craftPlayer = ((CraftPlayer)Regular.this.getOwner()).getHandle();
                if (craftPlayer.dn() != Regular.this.getPlayerLocation().getYaw() || Main.getPitch(craftPlayer) != Regular.this.getPlayerLocation().getPitch()) {
                    AtomicInteger teleportCounter = new AtomicInteger(0);
                    int teleportId = teleportCounter.incrementAndGet();
                    PacketPlayOutPosition packet1 = new PacketPlayOutPosition(craftPlayer.dc(), craftPlayer.de(), craftPlayer.di(), Regular.this.getPlayerLocation().getYaw(), Regular.this.getPlayerLocation().getPitch(), EnumSet.noneOf(PacketPlayOutPosition.EnumPlayerTeleportFlags.class), teleportId, false);
                    craftPlayer.b.a(packet1);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 1L);
        (new BukkitRunnable() {
            double time = 0.0D;

            long startTime = System.currentTimeMillis();

            public void run() {
                float x, z;
                if (System.currentTimeMillis() - this.startTime > 3000L) {
                    Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                        setOpeningState(false);
                    }, 2L);
                    Main.handler.playSound(Regular.this.getOwner(), Regular.this.getOwner().getLocation(), XSound.ENTITY_CAT_AMBIENT.name(), 20.0F, 5.0F);
                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(Regular.this.getOwner(), Regular.this.getInstance());
                    Bukkit.getPluginManager().callEvent((Event)event);
                    Regular.this.remove();
                    Main.handler.setFakeGameMode(Regular.this.getOwner(), GameMode.SURVIVAL);
                    Main.handler.hideButtonsFromPlayer(Regular.this.getOwner(), ButtonType.FUNCTIONAL, false);
                    Regular.this.setClickable(true);
                    ParticleType.of("EXPLOSION_HUGE").spawn(Regular.this.getOwner(), Regular.this.getLoc().clone().add(0.0D, -3.2D, 0.0D), 2, 0.5D, 0.5D, 0.5D, 0.0D);
                    Main.handler.playSound(Regular.this.getOwner(), Regular.this.getOwner().getLocation(), XSound.ENTITY_GENERIC_EXPLODE.name(), 20.0F, 5.0F);
                    new Regular(Regular.this.getLoc(), Regular.this.getOwner(), Regular.this.getIcon(), Regular.this.getConfig(), Regular.this.getBoxId(), Regular.this.getPlayerInitialLoc());
                    cancel();
                    return;
                }
                if (Regular.this.getBox() == null) {
                    cancel();
                    return;
                }
                Main.handler.playSound(Regular.this.getOwner(), Regular.this.getOwner().getLocation(), XSound.ENTITY_CHICKEN_EGG.name(), 20.0F, 5.0F);
                ParticleType.of("CLOUD").spawn(Regular.this.getOwner(), Regular.this.getLoc().clone().add(0.0D, -2.8D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                this.time++;
                DataWatcher dataWatcher = Regular.this.getBox().ai();
                if (this.time % 4.0D == 0.0D) {
                    x = 6.0F;
                    z = 6.0F;
                } else if (this.time % 4.0D == 1.0D) {
                    x = -6.0F;
                    z = 6.0F;
                } else if (this.time % 4.0D == 2.0D) {
                    x = -6.0F;
                    z = -6.0F;
                } else {
                    x = 6.0F;
                    z = -6.0F;
                }
                Vector3f pose = new Vector3f(x, (float)this.time * 18.0F, z);
                dataWatcher.b(new DataWatcherObject(16, DataWatcherRegistry.k), pose);
                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(Regular.this.getBox().ae(), dataWatcher, true);
                (((CraftPlayer)Regular.this.getOwner()).getHandle()).b.a((Packet)packet);
            }
        }).runTaskTimer(Main.handler.plugin, 20L, 1L);
        return true;
    }

    public void remove() {
        super.remove();
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { this.sword.ae() });
        (((CraftPlayer)getOwner()).getHandle()).b.a(packet);
        this.sword = null;
    }

    private void moveUp() {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            public void run() {
                if (System.currentTimeMillis() - this.startTime > 100L)
                    cancel();
                Animations.MoveUp(Regular.this.getOwner(), Regular.this.sword, 0.06F);
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 1L);
    }
}


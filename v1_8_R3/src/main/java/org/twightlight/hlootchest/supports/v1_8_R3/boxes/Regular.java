package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;
import org.twightlight.hlootchest.supports.v1_8_R3.utilities.Animations;

import java.util.HashSet;

public class Regular extends BoxManager {

    private EntityArmorStand sword;

    public Regular(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        super(location, player, icon, config, boxid, initialLocation);

        Location loc = Main.handler.stringToLocation(config.getString(boxid+".settings.decoration.location"));

        this.sword = createArmorStand(loc, "", false);

        Main.rotate(sword, config, boxid+".settings.decoration");

        sendSpawnPacket(getOwner(), sword);
        net.minecraft.server.v1_8_R3.ItemStack icon1 = CraftItemStack.asNMSCopy(new ItemStack(XMaterial.DIAMOND_SWORD.parseMaterial()));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                sword.getId(),
                0,
                icon1
        );

        ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(packet);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getBox().locY < loc.clone().getY() - 5.4) {
                    cancel();
                }
                Animations.MoveUp(getOwner(), sword, (float) -0.2);
                Animations.MoveUp(getOwner(), getBox(), (float) -0.2);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
    }

    @Override
    public boolean open() {

        if (!super.open()) {
            return false;
        }

        setClickable(false);
        moveUp();
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .with(FireworkEffect.Type.BURST)
                .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                .withFade(Color.GREEN, Color.BLUE)
                .withTrail()
                .build();
        Animations.spawnFireWork(getOwner(), getOwner().getLocation(), effect);
        Main.handler.setFakeGameMode(getOwner(), GameMode.SPECTATOR);

        Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 3500) {
                    return;
                }
                EntityPlayer craftPlayer = ((CraftPlayer) getOwner()).getHandle();
                if (craftPlayer.yaw != getPlayerLocation().getYaw() || craftPlayer.pitch != getPlayerLocation().getPitch()) {
                    PacketPlayOutPosition packet1 = new PacketPlayOutPosition(
                            craftPlayer.locX,
                            craftPlayer.locY,
                            craftPlayer.locZ,
                            getPlayerLocation().getYaw(),
                            getPlayerLocation().getPitch(),
                            new HashSet<>()
                    );
                    craftPlayer.playerConnection.sendPacket(packet1);
                }
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
        new BukkitRunnable() {
            double time = 0;
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 3000) {

                    Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_CAT_AMBIENT.name(), 20, 5);

                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
                    Bukkit.getPluginManager().callEvent(event);

                    remove();

                    Main.handler.setFakeGameMode(getOwner(), GameMode.SURVIVAL);

                    Main.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);

                    setClickable(true);

                    ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), getLoc().clone().add(0, -3.2, 0), 2, 0.5, 0.5, 0.5, 0);

                    Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_GENERIC_EXPLODE.name(), 20, 5);

                    new Regular(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId(), getPlayerInitialLoc());
                    cancel();
                    return;
                }
                if (getBox() == null) {
                    cancel();
                    return;
                }

                Main.handler.playSound(getOwner(), getOwner().getLocation(), XSound.ENTITY_CHICKEN_EGG.name(), 20, 5);

                ParticleType.of("CLOUD").spawn(getOwner(), getLoc().clone().add(0, -2.8, 0), 1, 0, 0, 0, 0);
                time += 1;
                DataWatcher dataWatcher = getBox().getDataWatcher();
                float x;
                float z;
                if (time%4 == 0) {
                    x = 6;
                    z = 6;
                } else if (time%4 == 1) {
                    x = -6;
                    z = 6;
                } else if (time%4 == 2) {
                    x = -6;
                    z = -6;
                } else {
                    x = 6;
                    z = -6;
                }
                Vector3f pose = new Vector3f(x, (float) time*18, z);
                dataWatcher.watch(11, pose);
                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(getBox().getId(), dataWatcher, true);

                ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(packet);
            }
        }.runTaskTimer(Main.handler.plugin, 20L, 1L);
        return true;
    }

    @Override
    public void remove() {
        super.remove();
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(sword.getId());
        ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(packet);
        sword = null;
    }

    private void moveUp() {
        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 100) {
                    cancel();
                }
                Animations.MoveUp(getOwner(), sword, (float) 0.06);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, 1L);
    }
}

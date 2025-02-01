package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.PlayerRewardGiveEvent;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveUp;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;

import java.util.HashSet;

public class Regular extends BoxManager {

    private EntityArmorStand sword;

    public Regular(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        super(location, player, icon, config, boxid, initialLocation);

        Location loc = v1_8_R3.handler.stringToLocation(config.getString(boxid+".settings.decoration.location"));

        this.sword = createArmorStand(loc, "", false);

        v1_8_R3.rotate(sword, config, boxid+".settings.decoration");

        sendSpawnPacket(getOwner(), sword);
        net.minecraft.server.v1_8_R3.ItemStack icon1 = CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SWORD));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                sword.getId(),
                0,
                icon1
        );

        ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(packet);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (v1_8_R3.handler.getBoxFromPlayer(getOwner()) != getInstance()) {
                    cancel();
                }
                ParticleType.of("CLOUD").spawn(getOwner(), getLoc().clone().add(0, -1.2, 0), 2, 1, 1, 1, 1);
            }
        }.runTaskTimer(v1_8_R3.handler.plugin, 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getBox().locY < loc.clone().getY() - 3.4) {
                    cancel();
                }
                new MoveUp(getOwner(), sword, (float) -0.2);
                new MoveUp(getOwner(), getBox(), (float) -0.2);
            }
        }.runTaskTimer(v1_8_R3.handler.plugin, 0L, 1L);
    }

    @Override
    public void open() {
        super.open();
        setClickable(false);
        moveUp();

        ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 3));
        summonFirework(getOwner());

        v1_8_R3.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, true);
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
        }.runTaskTimer(v1_8_R3.handler.plugin, 0L, 1L);
        new BukkitRunnable() {
            double time = 0;
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 3000) {

                    ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 0));
                    PlayerRewardGiveEvent event = new PlayerRewardGiveEvent(getOwner(), getInstance());
                    Bukkit.getPluginManager().callEvent(event);

                    remove();

                    v1_8_R3.handler.hideButtonsFromPlayer(getOwner(), ButtonType.FUNCTIONAL, false);
                    setClickable(true);
                    ParticleType.of("EXPLOSION_HUGE").spawn(getOwner(), getLoc().clone().add(0, -1.2, 0), 5, 1, 1, 1, 0);
                    new Regular(getLoc(), getOwner(), getIcon(), getConfig(), getBoxId(), getPlayerInitialLoc());
                    cancel();
                    return;
                }
                if (getBox() == null) {
                    cancel();
                    return;
                }
                ParticleType.of("CLOUD").spawn(getOwner(), getLoc().clone().add(0, -0.8, 0), 5, 0, 0, 0, 0);
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
        }.runTaskTimer(v1_8_R3.handler.plugin, 20L, 1L);
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
                new MoveUp(getOwner(), sword, (float) 0.06);
            }
        }.runTaskTimer(v1_8_R3.handler.plugin, 0L, 1L);
    }

    public EntityArmorStand getSword() {
        return this.sword;
    }

    public void summonFirework(Player targetPlayer) {
        EntityFireworks fireworkEntity = new EntityFireworks(((CraftWorld) targetPlayer.getLocation().getWorld()).getHandle());

        fireworkEntity.setPosition(targetPlayer.getLocation().getX(), targetPlayer.getLocation().getY(), targetPlayer.getLocation().getZ());

        NBTTagCompound nbtTag = new NBTTagCompound();

        nbtTag.setByte("Flight", (byte) 0);

        NBTTagList explosions = new NBTTagList();
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setIntArray("Colors", new int[] {0xFF0000});
        explosion.setByte("Type", (byte) 1);
        explosions.add(explosion);
        nbtTag.set("Explosions", explosions);


        fireworkEntity.a(nbtTag);

        PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(fireworkEntity, (byte) 17);
        ((CraftPlayer) targetPlayer).getHandle().playerConnection.sendPacket(packet);

    }

}

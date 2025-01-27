package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import fr.mrmicky.fastparticles.ParticleType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.BoxType;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveUp;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;

public class Regular extends BoxManager{

    EntityArmorStand sword;

    public Regular(Player player, Location location, ItemStack icon, BoxType boxType) {
        super(player, location, icon, BoxType.REGULAR);
        Location loc = location.clone().add(0, 1.1, 0);
        this.sword = createArmorStand(loc);
        Vector3f pose = new Vector3f(90, 90, 0);
        this.sword.setRightArmPose(pose);
        sendSpawnPacket(getOwner(), sword);
        net.minecraft.server.v1_8_R3.ItemStack icon1 = CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SWORD));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                sword.getId(),
                0,
                icon1
        );
        ((CraftPlayer) getOwner()).getHandle().playerConnection.sendPacket(packet);
        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 550) {
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
        new BukkitRunnable() {
            double time = 0;
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > 3000) {
                    remove();
                    setClickable(true);
                    ParticleType.of("FLAME").spawn(getOwner(), getLoc().clone().add(0, -1.2, 0), 40, 2, 2, 2);
                    new Regular(getOwner(), getLoc(), getIcon(), BoxType.REGULAR);
                    cancel();
                    return;
                }
                if (getBox() == null) {
                    cancel();
                    return;
                }
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
                if (System.currentTimeMillis() - startTime > 800) {
                    cancel();
                }
                new MoveUp(getOwner(), sword, (float) 0.1);
            }
        }.runTaskTimer(v1_8_R3.handler.plugin, 0L, 1L);
    }

    public EntityArmorStand getSword() {
        return this.sword;
    }

}

package org.twightlight.hlootchest.supports.v1_8_R3.utilities;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Firework {
    public static void spawnFirework(Location loc, Player player) {
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        EntityFireworks firework = new EntityFireworks(world);

        firework.setPosition(loc.getX(), loc.getY(), loc.getZ());

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fireworks = new NBTTagCompound();
        NBTTagList explosions = new NBTTagList();

        NBTTagCompound effect = new NBTTagCompound();
        effect.setInt("Type", 1); // 0 = Small Ball, 1 = Large Ball, 2 = Star, 3 = Creeper, 4 = Burst
        effect.setIntArray("Colors", new int[]{Color.RED.asRGB()});
        effect.setBoolean("Flicker", true);

        explosions.add(effect);
        fireworks.set("Explosions", explosions);
        fireworks.setByte("Flight", (byte) 1);
        tag.set("Fireworks", fireworks);

        firework.a(tag); // Set NBT data

        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(firework, 76);
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(firework.getId(), firework.getDataWatcher(), true);
        PacketPlayOutEntityStatus explodePacket = new PacketPlayOutEntityStatus(firework, (byte) 17);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metaPacket);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(explodePacket);
    }
}

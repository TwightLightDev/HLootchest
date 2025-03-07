package org.twightlight.hlootchest.supports.v1_19_R3.supports;

import com.cryptomorin.xseries.XSound;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class PacketEventsSupport {

    public void spawnLightning(Player p, Location loc) {
        UUID uuid = UUID.randomUUID();
        int entityId = SpigotReflectionUtil.generateEntityId();
        com.github.retrooper.packetevents.protocol.world.Location location = SpigotConversionUtil.fromBukkitLocation(loc);
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(entityId, uuid, EntityTypes.LIGHTNING_BOLT, location, loc.getYaw(), 0, null);

        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);

        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
            WrapperPlayServerDestroyEntities packetdestroy = new WrapperPlayServerDestroyEntities(entityId);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, packetdestroy);
        }, 5L);
    }

    public void setGameMode(Player p, GameMode gameMode) {
        int gameModeInt = 0;
        switch (gameMode) {
            case CREATIVE:
                gameModeInt = 1;
                break;
            case ADVENTURE:
                gameModeInt = 2;
                break;
            case SPECTATOR:
                gameModeInt = 3;
                break;
        }

        WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, gameModeInt);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
    }

    public void teleport(Player p, Location loc) {
        Random random = new Random();
        EntityPositionData pos = new EntityPositionData(new Vector3d().withX(loc.getX()).withY(loc.getY()).withZ(loc.getZ()), new Vector3d(), loc.getYaw(), loc.getPitch());
        WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(
                random.nextInt() | Integer.MIN_VALUE,
                pos,
                RelativeFlag.NONE);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
    }

    public void spawnFirework(Player p, Location loc, FireworkEffect fireworkEffect) {
        ItemStack sF = new org.bukkit.inventory.ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta) sF.getItemMeta();

        fireworkMeta.addEffect(fireworkEffect);
        fireworkMeta.setPower(2);
        sF.setItemMeta(fireworkMeta);

        UUID uuid = UUID.randomUUID();
        int entityId = SpigotReflectionUtil.generateEntityId();
        com.github.retrooper.packetevents.protocol.world.Location location = SpigotConversionUtil.fromBukkitLocation(loc);
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(entityId, uuid, EntityTypes.FIREWORK_ROCKET, location, loc.getYaw(), 0, null);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);

        EntityData metadata = new EntityData(8, EntityDataTypes.ITEMSTACK, SpigotReflectionUtil.decodeBukkitItemStack(sF));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, Collections.singletonList(metadata));
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);

        WrapperPlayServerEntityStatus statusPacket = new WrapperPlayServerEntityStatus(entityId, 17);
        PacketEvents.getAPI().getPlayerManager().sendPacket(p, statusPacket);
        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, destroyPacket);
        }, 1L);
        p.playSound(loc, XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(), 10.0F, 10.0F);
    }
}

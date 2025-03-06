package org.twightlight.hlootchest.supports.v1_19_R3.supports;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.UUID;


public class ProtocolLib {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public void spawnFakeFirework(Player player, Location location, FireworkEffect effect) {
        Main.api.getDebugService().sendDebugMsg(player, "Sending packet from v1_19_R3");
        int entityId = (int) (Math.random() * Integer.MAX_VALUE);
        UUID uuid = UUID.randomUUID();

        ItemStack fireworkItem = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) fireworkItem.getItemMeta();
        meta.addEffect(effect);
        meta.setPower(2);
        fireworkItem.setItemMeta(meta);

        PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, uuid);
        spawnPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        spawnPacket.getIntegers().write(1, (int) EntityType.FIREWORK.getTypeId());

        PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entityId);

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer itemSerializer = WrappedDataWatcher.Registry.getItemStackSerializer(false);
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, itemSerializer), fireworkItem);

        metadataPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        PacketContainer statusPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_STATUS);
        statusPacket.getIntegers().write(0, entityId);
        statusPacket.getBytes().write(0, (byte) 17);

        try {
            protocolManager.sendServerPacket(player, spawnPacket);
            protocolManager.sendServerPacket(player, metadataPacket);
            protocolManager.sendServerPacket(player, statusPacket);

            Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                destroyPacket.getIntegerArrays().write(0, new int[]{entityId});
                protocolManager.sendServerPacket(player, destroyPacket);

            }, 1L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void summonLightning(Player player, Location location) {
        PacketContainer spawnPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY);

        int entityId = player.getEntityId() + 5000;
        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, UUID.randomUUID());

        spawnPacket.getIntegers().write(6, 93);

        spawnPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        try {
            protocolManager.sendServerPacket(player, spawnPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> removeEntity(player, entityId), 5L);
    }

    public void removeEntity(Player player, int entityId) {
        PacketContainer destroyPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntegerArrays().write(0, new int[]{entityId});

        try {
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

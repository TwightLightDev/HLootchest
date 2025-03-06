package org.twightlight.hlootchest.supports.v1_21_R3.supports;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.UUID;

public class ProtocolLib extends org.twightlight.hlootchest.supports.v1_19_R3.supports.ProtocolLib {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public void setFakeGameMode(Player player, GameMode gameMode) {
        float modeValue;
        switch (gameMode) {
            case SURVIVAL: modeValue = 0.0F; break;
            case CREATIVE: modeValue = 1.0F; break;
            case ADVENTURE: modeValue = 2.0F; break;
            case SPECTATOR: modeValue = 3.0F; break;
            default: return;
        }

        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.GAME_STATE_CHANGE);
        packet.getIntegers().writeSafely(0, 3);
        packet.getFloat().writeSafely(0, modeValue);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlayerLocation(Player player, Location loc) {
        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.POSITION);

        packet.getDoubles().write(0, loc.getX());
        packet.getDoubles().write(1, loc.getY());
        packet.getDoubles().write(2, loc.getZ());

        packet.getFloat().write(0, loc.getYaw());
        packet.getFloat().write(1, loc.getPitch());

        packet.getBytes().write(0, (byte) (0x01 | 0x02));

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnFakeFirework(Player player, Location location, FireworkEffect effect) {
        try {
            int entityId = (int) (Math.random() * Integer.MAX_VALUE);

            ItemStack fireworkItem = new ItemStack(Material.FIREWORK_ROCKET);
            FireworkMeta meta = (FireworkMeta) fireworkItem.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(1);
            fireworkItem.setItemMeta(meta);

            PacketContainer spawnPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY);
            spawnPacket.getIntegers().write(0, entityId);
            spawnPacket.getUUIDs().write(0, UUID.randomUUID());
            spawnPacket.getEntityTypeModifier().write(0, EntityType.FIREWORK_ROCKET);
            spawnPacket.getDoubles()
                    .write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());

            PacketContainer metaPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA);
            metaPacket.getIntegers().write(0, entityId);
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            WrappedDataWatcher.WrappedDataWatcherObject itemMeta = new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false));
            watcher.setObject(itemMeta, fireworkItem);
            metaPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            PacketContainer explodePacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_STATUS);
            explodePacket.getIntegers().write(0, entityId);
            explodePacket.getBytes().write(0, (byte) 17);

            PacketContainer destroyPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, java.util.Collections.singletonList(entityId));

            protocolManager.sendServerPacket(player, spawnPacket);
            protocolManager.sendServerPacket(player, metaPacket);
            protocolManager.sendServerPacket(player, explodePacket);

            org.bukkit.Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> {
                try {
                    protocolManager.sendServerPacket(player, destroyPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

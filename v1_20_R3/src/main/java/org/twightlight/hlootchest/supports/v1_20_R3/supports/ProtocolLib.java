package org.twightlight.hlootchest.supports.v1_20_R3.supports;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.*;
import org.bukkit.entity.Player;

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

    @Override
    public void summonLightning(Player player, Location location) {
        PacketContainer spawnPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY);

        int entityId = (int) (Math.random() * Integer.MAX_VALUE);
        UUID entityUUID = UUID.randomUUID();

        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, entityUUID);
        spawnPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        spawnPacket.getIntegers().write(1, 93);

        try {
            protocolManager.sendServerPacket(player, spawnPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskLater(Main.handler.plugin, () -> removeEntity(player, entityId), 5L);

    }

    @Override
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

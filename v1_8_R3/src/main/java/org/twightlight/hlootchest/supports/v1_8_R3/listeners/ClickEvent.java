package org.twightlight.hlootchest.supports.v1_8_R3.listeners;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.twightlight.hlootchest.api.events.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.Regular;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;

import java.lang.reflect.Field;

public class ClickEvent extends PlayerConnection {
    NMSHandler nsm = v1_8_R3.handler;

    public ClickEvent(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        super.a(packet);

        int entityId = getEntityId(packet);
        TButton button = nsm.getButtonFromId(entityId);

        PacketPlayInUseEntity.EnumEntityUseAction action = packet.a();
        if (button != null && button.isClickable()) {
            handleButtonInteraction(action, button, entityId);
        }
        BoxManager box = BoxManager.boxlists.get(entityId);
        if (box != null && box.isClickable()) {
            handleButtonInteraction(action, box);
        }

    }

    private void handleButtonInteraction(PacketPlayInUseEntity.EnumEntityUseAction action, TButton button, int entityId) {
        PlayerButtonClickEvent event = new PlayerButtonClickEvent(this.player.getBukkitEntity(), button);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        if (action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
            
        }
    }

    private void handleButtonInteraction(PacketPlayInUseEntity.EnumEntityUseAction action, BoxManager box) {
        PlayerOpenLCEvent event = new PlayerOpenLCEvent(this.player.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        if (action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
            if (box instanceof Regular) {
                Regular box1 = (Regular) box;
                box1.open();
            }
        }
    }

    private int getEntityId(PacketPlayInUseEntity packet) {
        try {
            Field field = packet.getClass().getDeclaredField("a");
            field.setAccessible(true);

            return field.getInt(packet);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

package org.twightlight.hlootchest.supports.v1_8_R3.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_8_R3.buttons.Button;

import java.lang.reflect.Field;
import java.util.List;

public class ClickEvent extends PlayerConnection {
    NMSHandler nms = Main.handler;

    public ClickEvent(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        super.a(packet);

        int entityId = getEntityId(packet);
        TButton button = nms.getButtonFromId(entityId);

        PacketPlayInUseEntity.EnumEntityUseAction action = packet.a();
        if (button != null && button.isClickable()) {
            handleButtonInteraction(action, button);
        }
        TBox box = BoxManager.boxlists.get(entityId);
        if (box != null && box.isClickable() && box.isClickToOpen()) {
            handleButtonInteraction(action, box);
        }

    }

    private void handleButtonInteraction(PacketPlayInUseEntity.EnumEntityUseAction action, TButton button) {
        PlayerButtonClickEvent event = new PlayerButtonClickEvent(this.player.getBukkitEntity(), button);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || button.isHiding()) {
            return;
        }

        if (action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
            if (((Button) button).getSound() != null) {
                Main.handler.playSound(player.getBukkitEntity(), player.getBukkitEntity().getLocation(), ((Button) button).getSound().getSoundString(), ((Button) button).getSound().getYaw(), ((Button) button).getSound().getPitch());
            }
            List<String> actions = button.getActions();
            for (String stringAction : actions) {
                String[] dataset = stringAction.split(" ", 2);
                if (dataset[0].equals("[player]")) {
                    player.getBukkitEntity().performCommand(dataset[1].replace("{player}", this.player.getBukkitEntity().getName()));
                } else if (dataset[0].equals("[console]")) {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.getServer().dispatchCommand(console, dataset[1].replace("{player}", this.player.getBukkitEntity().getName()));
                } else if (dataset[0].equals("[message]")) {
                    player.getBukkitEntity().sendMessage(
                            ChatColor.translateAlternateColorCodes('&'
                            , PlaceholderAPI.setPlaceholders(player.getBukkitEntity()
                            , dataset[1])));
                } else if ((dataset[0].equals("[open]"))) {
                    Main.handler.getBoxFromPlayer(player.getBukkitEntity()).open();
                } else if ((dataset[0].equals("[close]"))) {
                    Main.api.getSessionUtil().getSessionFromPlayer(player.getBukkitEntity()).close();
                }
            }
        }
    }

    private void handleButtonInteraction(PacketPlayInUseEntity.EnumEntityUseAction action, TBox box) {

        if (!box.isClickToOpen()) {
            return;
        }
        if (action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
            box.open();
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

package org.twightlight.hlootchest.supports.v1_17_R1.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_17_R1.Main;
import org.twightlight.hlootchest.supports.v1_17_R1.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_17_R1.buttons.Button;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ClickEvent extends PlayerConnection {
    NMSHandler nms = Main.handler;
    EntityPlayer player = this.b;

    public ClickEvent(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(((CraftServer) Bukkit.getServer()).getServer()
                , networkManager, entityPlayer);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        super.a(packet);

        int entityId = getEntityId(packet);
        TButton button = nms.getButtonFromId(entityId);

        String action = getAction(packet);

        if (button != null && button.isClickable()) {
            handleButtonInteraction(action, button);
        }
        TBox box = BoxManager.boxlists.get(entityId);
        if (box != null && box.isClickable() && box.isClickToOpen()) {
            handleButtonInteraction(action, box);
        }
    }

    private void handleButtonInteraction(String action, TButton button) {
        PlayerButtonClickEvent event = new PlayerButtonClickEvent(this.player.getBukkitEntity(), button);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || button.isHiding()) {
            return;
        }

        if (action.equals("ATTACK") || action.equals("INTERACT") || action.equals("INTERACT_AT")) {
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
                            ChatColor.translateAlternateColorCodes('&',
                                    PlaceholderAPI.setPlaceholders(player.getBukkitEntity(), dataset[1])));
                } else if ((dataset[0].equals("[open]"))) {
                    Main.handler.getBoxFromPlayer(player.getBukkitEntity()).open();
                } else if ((dataset[0].equals("[close]"))) {
                    TBox box = Main.handler.getBoxFromPlayer(player.getBukkitEntity());
                    box.removeVehicle(player.getBukkitEntity());
                    box.getOwner().teleport(box.getPlayerInitialLoc());
                    box.remove();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.equals(player.getBukkitEntity())) {
                            online.showPlayer(Main.handler.plugin, player.getBukkitEntity());
                        }
                    }
                    Main.handler.removeButtonsFromPlayer(player.getBukkitEntity(), ButtonType.FUNCTIONAL);
                    Main.handler.removeButtonsFromPlayer(player.getBukkitEntity(), ButtonType.REWARD);
                    player.getBukkitEntity().setGameMode(GameMode.SPECTATOR);
                    player.getBukkitEntity().setGameMode(GameMode.SURVIVAL);
                }
            }
        }
    }

    private void handleButtonInteraction(String action, TBox box) {
        if (!box.isClickToOpen()) {
            return;
        }
        if (action.equals("ATTACK") || action.equals("INTERACT") || action.equals("INTERACT_AT")) {
            box.open();
        }
    }

    private int getEntityId(PacketPlayInUseEntity packet) {
        try {
            Field field = packet.getClass().getDeclaredField("a");
            field.setAccessible(true);
            return field.getInt(packet);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return -1;
        }
    }

    private String getAction(PacketPlayInUseEntity packet) {
        try {
            Field field = PacketPlayInUseEntity.class.getDeclaredField("b");
            field.setAccessible(true);
            Object actionObj = field.get(packet);
            Method methodA = actionObj.getClass().getDeclaredMethod("a");
            methodA.setAccessible(true);
            Object result = methodA.invoke(actionObj);
            return ((Enum<?>) result).name();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}

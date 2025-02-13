// Decompiled with: CFR 0.152
// Class Version: 8
package org.twightlight.hlootchest.supports.v1_19_R3.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.PlayerButtonClickEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_19_R3.buttons.Button;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ClickEvent
        extends PlayerConnection {
    NMSHandler nms = Main.handler;
    EntityPlayer b = this.f();

    public ClickEvent(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super((MinecraftServer)((CraftServer)Bukkit.getServer()).getServer(), networkManager, entityPlayer);
    }

    public void a(PacketPlayInUseEntity packet) {
        TBox box;
        super.a(packet);
        int entityId = this.getEntityId(packet);
        TButton button = this.nms.getButtonFromId(entityId);
        String action = this.getAction(packet);
        if (button != null && button.isClickable()) {
            this.handleButtonInteraction(action, button);
        }
        if ((box = BoxManager.boxlists.get(entityId)) != null && box.isClickable() && box.isClickToOpen()) {
            this.handleButtonInteraction(action, box);
        }
    }

    private void handleButtonInteraction(String action, TButton button) {
        PlayerButtonClickEvent event = new PlayerButtonClickEvent((Player)this.b.getBukkitEntity(), button);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || button.isHiding()) {
            return;
        }
        if (action.equals("ATTACK") || action.equals("INTERACT") || action.equals("INTERACT_AT")) {
            if (((Button)button).getSound() != null) {
                Main.handler.playSound((Player)this.b.getBukkitEntity(), this.b.getBukkitEntity().getLocation(), ((Button)button).getSound().getSoundString(), ((Button)button).getSound().getYaw(), ((Button)button).getSound().getPitch());
            }
            List<String> actions = button.getActions();
            for (String stringAction : actions) {
                String[] dataset = stringAction.split(" ", 2);
                if (dataset[0].equals("[player]")) {
                    this.b.getBukkitEntity().performCommand(dataset[1].replace("{player}", this.b.getBukkitEntity().getName()));
                    continue;
                }
                if (dataset[0].equals("[console]")) {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.getServer().dispatchCommand(console, dataset[1].replace("{player}", this.b.getBukkitEntity().getName()));
                    continue;
                }
                if (dataset[0].equals("[message]")) {
                    this.b.getBukkitEntity().sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player)this.b.getBukkitEntity(), (String)dataset[1])));
                    continue;
                }
                if (dataset[0].equals("[open]")) {
                    Main.handler.getBoxFromPlayer((Player)this.b.getBukkitEntity()).open();
                    continue;
                }
                if (!dataset[0].equals("[close]")) continue;
                TBox box = Main.handler.getBoxFromPlayer((Player)this.b.getBukkitEntity());
                box.removeVehicle((Player)this.b.getBukkitEntity());
                box.getOwner().teleport(box.getPlayerInitialLoc());
                box.remove();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.equals(this.b.getBukkitEntity())) continue;
                    player.showPlayer(Main.handler.plugin, (Player)this.b.getBukkitEntity());
                }
                Main.handler.removeButtonsFromPlayer((Player)this.b.getBukkitEntity(), ButtonType.FUNCTIONAL);
                Main.handler.removeButtonsFromPlayer((Player)this.b.getBukkitEntity(), ButtonType.REWARD);
                this.b.getBukkitEntity().setGameMode(GameMode.SPECTATOR);
                this.b.getBukkitEntity().setGameMode(GameMode.SURVIVAL);
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
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            return -1;
        }
    }

    private String getAction(PacketPlayInUseEntity packet) {
        try {
            Field field = PacketPlayInUseEntity.class.getDeclaredField("b");
            field.setAccessible(true);
            Object actionObj = field.get(packet);
            Method methodA = actionObj.getClass().getDeclaredMethod("a", new Class[0]);
            methodA.setAccessible(true);
            Object result = methodA.invoke(actionObj, new Object[0]);
            return ((Enum)result).name();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }
}

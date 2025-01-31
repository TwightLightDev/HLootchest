package org.twightlight.hlootchest.supports.v1_8_R3.buttons;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveBackward;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveForward;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Button implements TButton {
    private final int id;
    private final Player owner;
    private final EntityArmorStand armorstand;
    private boolean isMoved = false;
    private final BukkitTask task;
    private boolean clickable = true;
    private boolean moveable = true;
    private ButtonType type;
    private List<String> actions;
    private Sound sound;

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    public Button(ButtonType type, Player player, ItemStack icon, String path, TConfigManager config) {
        this.owner = player;

        this.actions = config.getList(path+".actions");
        this.sound = Sound.valueOf(config.getString(path+".click-sound"));

        Location location = v1_8_R3.stringToLocation(config.getString(path + ".location"));

        EntityArmorStand armorStand = createArmorStand(location, config.getString(path+".name"), config.getBoolean(path+".enable-name"));
        this.id = armorStand.getId();

        this.armorstand = armorStand;
        v1_8_R3.rotate(armorstand, config, path);

        buttonIdMap.put(this.id, this);
        playerButtonMap.computeIfAbsent(player, k -> new ArrayList<>()).add(this);
        this.type = type;
        sendSpawnPacket(player, armorStand);
        equipIcon(armorStand, icon);

        this.task = Bukkit.getScheduler().runTaskTimer(v1_8_R3.handler.plugin, () -> {
            if (!owner.isOnline()) {
                cancelTask();
                return;
            }
            if (owner.getLocation().distance(armorstand.getBukkitEntity().getLocation()) > 5) {
                if (isMoved) {
                    moveBackward();
                    unmovablePeriod(500);
                    isMoved = false;
                }
                return;
            }

            Location playerEye = owner.getEyeLocation();
            Vector playerDirection = playerEye.getDirection();
            Vector playerPosition = playerEye.toVector();

            Vector armorStandPosition = new Vector(armorstand.locX, armorstand.locY + 1.6, armorstand.locZ);
            Vector toArmorStand = armorStandPosition.subtract(playerPosition).normalize();
            double dotProduct = playerDirection.dot(toArmorStand);

            if (dotProduct > 0.98) {
                if (!isMoved && moveable) {
                    moveForward();
                    unmovablePeriod(500);
                    isMoved = true;
                }
            } else {
                if (isMoved && moveable) {
                    moveBackward();
                    unmovablePeriod(500);
                    isMoved = false;
                }
            }}, 0L, 5L);

    }

    private EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);

        armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return armorStand;
    }

    private void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private void unmovablePeriod(int time) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.moveable = false;

        scheduler.schedule(() -> {
            moveable = true;
            scheduler.shutdown();
        }, time, TimeUnit.MILLISECONDS);
    }

    private void equipIcon(EntityArmorStand armorStand, ItemStack bukkiticon) {
        net.minecraft.server.v1_8_R3.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                armorStand.getId(),
                4,
                icon
        );
        ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
    }

    public void remove() {
        if (armorstand != null) {
            cancelTask();
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armorstand.getId());
            ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);

            buttonIdMap.remove(id);

            List<TButton> buttons = playerButtonMap.get(owner);
            if (buttons != null) {
                buttons.remove(this);
                playerButtonMap.put(owner, buttons);
            }
        }
    }

    public void moveForward() {
        new MoveForward(owner, armorstand, 1);
    }

    public void moveBackward() {
        new MoveBackward(owner, armorstand, 1);
    }

    public int getCustomId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public EntityArmorStand getEntity() {
        return armorstand;
    }

    public void cancelTask() {
        this.task.cancel();
    }

    public boolean isMoved() {
        return isMoved;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean bool) {
        this.clickable = bool;
    }

    public ButtonType getType() {
        return type;
    }

    public List<String> getActions() {
        return actions;
    }

    public Sound getSound() {
        return sound;
    }
}
package org.twightlight.hlootchest.supports.v1_8_R3.buttons;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.ButtonSpawnEvent;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveBackward;
import org.twightlight.hlootchest.supports.v1_8_R3.animations.MoveForward;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;

import java.util.*;
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
    private boolean isHiding = false;
    private ItemStack icon = null;
    private TConfigManager config;
    private String pathToButton;
    private String nameVisibleMode = "always";

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();
    public static final Map<TButton, List<EntityArmorStand>> linkedStands = new HashMap<>();
    public static final Map<EntityArmorStand, ItemStack> linkedStandsIcon = new HashMap<>();


    public Button(Location location, ButtonType type, Player player, ItemStack icon, String path, TConfigManager config) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;

        ButtonSpawnEvent event = new ButtonSpawnEvent(owner, this);
        Bukkit.getPluginManager().callEvent(event);

        this.actions = (config.getList(path+".actions") != null) ? config.getList(path+".actions") : new ArrayList<>();
        this.sound = Sound.valueOf(config.getString(path+".click-sound"));

        boolean enableName = (config.getYml().contains(path+".enable-name")) ? config.getBoolean(path+".enable-name") : false;
        if (config.getYml().contains(path+".name-visible-mode") && enableName) {
            String mode = config.getString(path+".name-visible-mode");
            if (mode.equals("hover")) {
                enableName = false;
            }
            nameVisibleMode = mode;
        }


        EntityArmorStand armorStand = createArmorStand(location, (config.getString(path+".name") != null) ? config.getString(path+".name"):"", enableName);
        this.id = armorStand.getId();

        this.armorstand = armorStand;
        v1_8_R3.rotate(armorstand, config, path);


        boolean rotateOnSpawn = (config.getYml().contains(path+".rotate-on-spawn")) ? config.getBoolean(path+".rotate-on-spawn") : false;

        buttonIdMap.put(this.id, this);
        playerButtonMap.computeIfAbsent(player, k -> new ArrayList<>()).add(this);
        linkedStands.put(this, new ArrayList<>());

        this.type = type;

        sendSpawnPacket(player, armorStand);

        if (rotateOnSpawn) {
            float angle = (float) (location.clone().getYaw() - config.getDouble(path+".final-degrees"));

            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!owner.isOnline() || i >= 8) {
                        this.cancel();
                        return;
                    }

                    i ++;

                    DataWatcher dataWatcher = armorstand.getDataWatcher();

                    Vector3f pose = new Vector3f(0, location.getYaw() - ((angle/8) * i), 0);
                    dataWatcher.watch(11, pose);
                    PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorstand.getId(), dataWatcher, true);
                    ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
                }
            }.runTaskTimer(v1_8_R3.handler.plugin, 10L, 1L);
        }

        boolean isHoldingIcon = (config.getYml().contains(path + ".holding-icon")) ? config.getBoolean(pathToButton + ".holding-icon") : true;
        if (isHoldingIcon) {
            equipIcon(armorStand, icon);
            this.icon = icon;
        }

        if (config.getYml().getConfigurationSection(path + ".children") != null) {

            Set<String> linkeds = config.getYml().getConfigurationSection(path + ".children").getKeys(false);

            for (String childname : linkeds) {
                String newpath = path + ".children" + "." + childname;
                Location childlocation = null;

                if (config.getString(newpath + ".location") != null) {
                    childlocation = v1_8_R3.handler.stringToLocation(config.getString(newpath + ".location"));
                } else if (config.getString(newpath + ".location-offset") != null) {
                    String[] offsetXYZ = config.getString(newpath + ".location-offset").split(",");
                    childlocation = location.clone().add(Double.parseDouble(offsetXYZ[0]), Double.parseDouble(offsetXYZ[1]), Double.parseDouble(offsetXYZ[2]));
                }

                if (childlocation != null) {
                    EntityArmorStand child = createArmorStand(childlocation, (config.getString(newpath + ".name") != null) ? config.getString(newpath + ".name") : "", config.getBoolean(newpath + ".enable-name"));
                    v1_8_R3.rotate(child, config, newpath);
                    linkedStands.get(this).add(child);
                    sendSpawnPacket(player, child);
                    if (config.getYml().contains(newpath + ".icon")) {
                        ItemStack childicon = v1_8_R3.createItem(Material.valueOf(config.getString(newpath + ".icon.material")), config.getString(newpath + ".icon.head_value"), config.getInt(newpath + ".icon.data"), "", new ArrayList<>(), false);
                        equipIcon(child, childicon);
                        linkedStandsIcon.put(child, childicon);
                    }
                }
            }
        }
        this.task = Bukkit.getScheduler().runTaskTimer(v1_8_R3.handler.plugin, () -> {
            if (!owner.isOnline()) {
                cancelTask();
                return;
            }
            if (owner.getLocation().distance(armorstand.getBukkitEntity().getLocation()) > 5 || isHiding) {
                if (isMoved) {
                    moveBackward();
                    if (nameVisibleMode.equals("hover")) {
                        sendNameVisibilityPacket(owner, armorstand, false);
                    }
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
                    if (nameVisibleMode.equals("hover")) {
                        sendNameVisibilityPacket(owner, armorstand, true);
                    }
                    unmovablePeriod(500);
                    isMoved = true;
                }
            } else {
                if (isMoved && moveable) {
                    moveBackward();
                    if (nameVisibleMode.equals("hover")) {
                        sendNameVisibilityPacket(owner, armorstand, false);
                    }
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

    private void sendDespawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armorStand.getId());
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
        if (bukkiticon != null) {
            net.minecraft.server.v1_8_R3.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            int slot = 4;
            if (type == ButtonType.REWARD) {
                slot = 0;
            }
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                    armorStand.getId(),
                    slot,
                    icon
            );
            ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void hide(boolean isHiding) {
        this.isHiding = isHiding;
        if (isHiding) {
            sendDespawnPacket(owner, armorstand);
            for (EntityArmorStand stand : linkedStands.get(this)) {
                sendDespawnPacket(owner, stand);
            }
        } else {
            sendSpawnPacket(owner, armorstand);
            equipIcon(armorstand, icon);
            for (EntityArmorStand stand : linkedStands.get(this)) {
                sendSpawnPacket(owner, stand);
                if (linkedStandsIcon.get(stand) != null) {
                    equipIcon(stand, linkedStandsIcon.get(stand));
                }
            }
        }
    }

    public void remove() {
        if (armorstand != null) {
            cancelTask();
            sendDespawnPacket(owner, armorstand);
            for (EntityArmorStand stand : linkedStands.get(this)) {
                sendDespawnPacket(owner, stand);
                linkedStandsIcon.remove(stand);
            }

            buttonIdMap.remove(id);
            linkedStands.remove(this);


            List<TButton> buttons = playerButtonMap.get(owner);
            if (buttons != null) {
                buttons.remove(this);
                playerButtonMap.put(owner, buttons);
            }
        }
    }

    public void moveForward() {
        new MoveForward(owner, armorstand, (float) 0.5);
        for (EntityArmorStand stand : linkedStands.get(this)) {
            new MoveForward(owner, stand, (float) 0.5);
        }
    }

    public void moveBackward() {
        new MoveBackward(owner, armorstand, (float) 0.5);
        for (EntityArmorStand stand : linkedStands.get(this)) {
            new MoveBackward(owner, stand, (float) 0.5);
        }
    }

    public void sendNameVisibilityPacket(Player player, EntityArmorStand armorStand, boolean visible) {
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);
        if (visible) {
            armorStand.setCustomNameVisible(true);
        } else {
            armorStand.setCustomNameVisible(false);
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
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

    public boolean isHiding() {
        return isHiding;
    }
}
package org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.lootchest.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.player.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager implements TBox {
    private Player owner;
    int id;
    private final ArmorStand box;
    private boolean clickable = true;
    private Location loc;
    private ItemStack icon;
    private TConfigManager config;
    private String boxid;
    private TBox instance;
    private boolean clickToOpen;
    private Location playerLocation;
    private List<Location> rewardsLocation = new ArrayList<Location>();
    private boolean isOpening;

    private static final Map<Player, Pig> vehicles = new HashMap<>();
    public static final ConcurrentHashMap<Integer, TBox> boxlists = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, TBox> boxPlayerlists = new ConcurrentHashMap<>();

    public BoxManager(Location location, Player player, ItemStack icon, TConfigManager config, String boxid) {
        Main.api.getSessionUtil().getSessionFromPlayer(player).setBox(this);
        this.owner = player;
        Location Plocation = Utility.stringToLocation(config.getString(boxid + ".settings.player-location"));

        playerLocation = Plocation;

        if (vehicles.get(owner) == null) {

            Pig vehicle = (Pig) Plocation.getWorld().spawnEntity(Plocation.clone().add(0, -0.3, 0), EntityType.PIG);

            vehicle.setCustomName("LootchestVehicle");
            vehicle.setCustomNameVisible(false);
            vehicle.setAI(false);
            vehicle.setSilent(true);
            vehicle.setCollidable(false);
            vehicle.setInvulnerable(true);
            vehicle.setGravity(false);
            vehicle.setVisibleByDefault(false);
            player.showEntity(Main.handler.plugin, vehicle);
            vehicle.setInvisible(true);


            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(Plocation);
                    vehicle.addPassenger(player);

                }
            }.runTaskLater(Main.handler.plugin, 2L);
            vehicles.put(owner, vehicle);
        }
        this.box = this.createArmorStand(location, "", false);
        this.clickToOpen = config.getBoolean(boxid + ".settings.click-to-open");
        this.instance = this;
        this.id = this.box.getEntityId();
        this.loc = location;
        this.icon = icon;
        this.config = config;
        this.boxid = boxid;
        Main.nmsUtil.rotate(this.box, config, boxid + ".settings");
        boxlists.put(this.id, this);
        boxPlayerlists.put(this.owner, this);
        sendSpawnPacket(this.owner, this.box);
        this.equipIcon(this.box, icon);
        if (config.getList(boxid + ".rewards-location") != null) {
            for (String string : config.getList(boxid + ".rewards-location")) {
                this.rewardsLocation.add(Utility.stringToLocation(string));
            }
        }

        LCSpawnEvent lCSpawnEvent = new LCSpawnEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent(lCSpawnEvent);
    }

    public ArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setGravity(false);
        armorStand.setRotation(location.getYaw(), location.getPitch());
        armorStand.setVisibleByDefault(false);

        return armorStand;
    }

    public static void sendSpawnPacket(Player player, ArmorStand armorStand) {
        player.showEntity(Main.handler.plugin, armorStand);
    }

    public void equipIcon(ItemStack bukkitIcon) {
        if (box != null) {
            box.getEquipment().setItem(EquipmentSlot.HAND, bukkitIcon);
        }
    }

    private void equipIcon(ArmorStand armorStand, ItemStack bukkitIcon) {
        armorStand.getEquipment().setItem(EquipmentSlot.HEAD, bukkitIcon);
    }

    public boolean open() {
        Main.handler.removeButtonsFromPlayer(this.owner, ButtonType.REWARD);
        PlayerOpenLCEvent event = new PlayerOpenLCEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public void remove() {
        if (this.box != null) {
            this.box.remove();
            boxlists.remove(this.id);
            boxPlayerlists.remove(this.owner);
        }
    }

    public boolean isClickable() {
        return this.clickable;
    }

    public void setClickable(boolean bool) {
        this.clickable = bool;
    }

    public Player getOwner() {
        return this.owner;
    }

    public ArmorStand getBox() {
        return this.box;
    }

    public Location getLoc() {
        return this.loc;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public TConfigManager getConfig() {
        return this.config;
    }

    public String getBoxId() {
        return this.boxid;
    }


    public Map<Player, Pig> getVehiclesList() {
        return vehicles;
    }

    public void removeVehicle(Player p) {
        if (vehicles.get(p) != null) {
            vehicles.get(p).remove();
            vehicles.remove(p);
        }
    }


    public void setOpeningState(Boolean state) {
        isOpening = state;
    }

    public boolean isOpening() {
        return isOpening;
    }

    public TBox getInstance() {
        return this.instance;
    }

    public boolean isClickToOpen() {
        return this.clickToOpen;
    }

    public Location getPlayerLocation() {
        return this.playerLocation;
    }

    public List<Location> getRewardsLocation() {
        return new ArrayList<Location>(this.rewardsLocation);
    }
}

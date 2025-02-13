// Decompiled with: CFR 0.152
// Class Version: 8
package org.twightlight.hlootchest.supports.v1_19_R3.boxes;

import com.cryptomorin.xseries.XPotion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager
        implements TBox {
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
    private Location initialLocation;
    private List<Location> rewardsLocation = new ArrayList<Location>();
    private static final Map<Player, Pig> vehicles = new HashMap<Player, Pig>();
    public static final ConcurrentHashMap<Integer, TBox> boxlists = new ConcurrentHashMap();
    public static final ConcurrentHashMap<Player, TBox> boxPlayerlists = new ConcurrentHashMap();

    public BoxManager(Location location, Player player, ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        Location Plocation;
        this.owner = player;
        this.initialLocation = initialLocation;
        this.box = this.createArmorStand(location, "", false);
        this.clickToOpen = config.getBoolean(boxid + ".settings.click-to-open");
        this.instance = this;
        this.id = this.box.getEntityId();
        this.loc = location;
        this.icon = icon;
        this.config = config;
        this.boxid = boxid;
        Main.rotate(this.box, config, boxid + ".settings");
        boxlists.put(this.id, this);
        boxPlayerlists.put(this.owner, this);
        sendSpawnPacket(this.owner, this.box);
        this.equipIcon(this.box, icon);
        if (config.getList(boxid + ".rewards-location") != null) {
            for (String string : config.getList(boxid + ".rewards-location")) {
                this.rewardsLocation.add(Main.handler.stringToLocation(string));
            }
        }
        this.playerLocation = Plocation = Main.handler.stringToLocation(config.getString(boxid + ".settings.player-location"));
        if (vehicles.get(this.owner) == null) {
            this.owner.teleport(Plocation);
            for (Player player2 : Bukkit.getOnlinePlayers()) {
                if (player2.equals(this.owner)) continue;
                player2.hidePlayer(Main.handler.plugin, this.owner);
            }
            Pig pig = (Pig)Plocation.getWorld().spawnEntity(Plocation.clone().add(0.0, -0.3, 0.0), EntityType.PIG);
            pig.addPotionEffect(new PotionEffect(XPotion.INVISIBILITY.getPotionEffectType(), Integer.MAX_VALUE, 1, false, false));
            pig.setCustomName("LootchestVehicle");
            pig.setCustomNameVisible(false);
            pig.setAI(false);
            pig.setInvulnerable(true);
            pig.addPassenger(this.owner);
            vehicles.put(this.owner, pig);
        } else {
            vehicles.get(this.owner).addPassenger(owner);
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
        return armorStand;
    }

    public static void sendSpawnPacket(Player player, ArmorStand armorStand) {
        player.showEntity(Main.handler.plugin, armorStand);
    }

    @Override
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

    @Override
    public boolean isClickable() {
        return this.clickable;
    }

    @Override
    public void setClickable(boolean bool) {
        this.clickable = bool;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    public ArmorStand getBox() {
        return this.box;
    }

    @Override
    public Location getLoc() {
        return this.loc;
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public TConfigManager getConfig() {
        return this.config;
    }

    @Override
    public String getBoxId() {
        return this.boxid;
    }

    @Override
    public Location getPlayerInitialLoc() {
        return this.initialLocation;
    }

    @Override
    public Map<Player, Pig> getVehiclesList() {
        return vehicles;
    }

    @Override
    public void removeVehicle(Player p) {
        if (vehicles.get(p) != null) {
            vehicles.get(p).remove();
            vehicles.remove(p);
        }
    }

    @Override
    public TBox getInstance() {
        return this.instance;
    }

    @Override
    public boolean isClickToOpen() {
        return this.clickToOpen;
    }

    @Override
    public Location getPlayerLocation() {
        return this.playerLocation;
    }

    @Override
    public List<Location> getRewardsLocation() {
        return new ArrayList<Location>(this.rewardsLocation);
    }
}

package org.twightlight.hlootchest.supports.v1_18_R2.boxes;

import com.cryptomorin.xseries.XPotion;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_18_R2.Main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager implements TBox {
    private Player owner;

    int id;

    private EntityArmorStand box;

    private boolean clickable = true;

    private Location loc;

    private org.bukkit.inventory.ItemStack icon;

    private TConfigManager config;

    private String boxid;

    private TBox instance;

    private boolean clickToOpen;

    private Location playerLocation;

    private Location initialLocation;

    private List<Location> rewardsLocation = new ArrayList<>();

    private static final Map<Player, Pig> vehicles = new HashMap<>();

    public static final ConcurrentHashMap<Integer, TBox> boxlists = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Player, TBox> boxPlayerlists = new ConcurrentHashMap<>();

    private boolean isOpening;

    public BoxManager(Location location, Player player, org.bukkit.inventory.ItemStack icon, TConfigManager config, String boxid, Location initialLocation) {
        Main.api.getSessionUtil().getSessionFromPlayer(player).setNewBox(this);
        owner = player;
        this.initialLocation = initialLocation;
        this.box = createArmorStand(location, "", false);
        this.clickToOpen = config.getBoolean(boxid + ".settings.click-to-open");
        this.instance = this;
        this.id = box.ae();
        this.loc = location;
        this.icon = icon;
        this.config = config;
        this.boxid = boxid;
        Main.rotate(box, config, boxid + ".settings");
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(box.ae(), box.ai(), true);
        (((CraftPlayer)owner).getHandle()).b.a(metadataPacket);
        boxlists.put(Integer.valueOf(id), this);
        boxPlayerlists.put(owner, this);
        sendSpawnPacket(owner, box);
        equipIcon(box, icon);
        if (config.getList(boxid + ".rewards-location") != null)
            for (String reward : config.getList(boxid + ".rewards-location"))
                rewardsLocation.add(Main.handler.stringToLocation(reward));
        Location Plocation = Main.handler.stringToLocation(config.getString(boxid + ".settings.player-location"));
        playerLocation = Plocation;
        if (vehicles.get(owner) == null) {
            owner.teleport(Plocation);
            owner.setVelocity(new Vector(0, 0, 0));
            Chunk chunk = Plocation.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(owner))
                    online.hidePlayer(Main.handler.plugin, owner);
            }
            Pig vehicle = (Pig)Plocation.getWorld().spawnEntity(Plocation.clone().add(0.0D, -0.3D, 0.0D), EntityType.PIG);
            vehicle.setInvisible(true);
            vehicle.setCustomName("LootchestVehicle");
            vehicle.setCustomNameVisible(false);
            vehicle.setSilent(true);
            vehicle.setCollidable(false);
            vehicle.setGravity(false);
            vehicle.setAI(false);
            vehicle.setInvulnerable(true);
            vehicle.addPassenger(owner);
            vehicles.put(owner, vehicle);

        }
        LCSpawnEvent event = new LCSpawnEvent(owner, this);
        Bukkit.getPluginManager().callEvent((Event)event);
    }

    public EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand((World)nmsWorld, location.getX(), location.getY(), location.getZ());
        armorStand.n(isNameEnable);
        armorStand.a(IChatBaseComponent.a(Main.p(owner, ChatColor.translateAlternateColorCodes('&', name))));
        armorStand.j(true);
        armorStand.e(true);
        armorStand.o(location.getYaw());
        armorStand.p(location.getPitch());
        return armorStand;
    }

    public void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity((Entity)armorStand);
        (((CraftPlayer)player).getHandle()).b.a(packet);
        armorStand.ai().b(new DataWatcherObject(0, DataWatcherRegistry.a), Byte.valueOf((byte)32));
        PacketPlayOutEntityMetadata packet1 = new PacketPlayOutEntityMetadata(armorStand.ae(), armorStand.ai(), true);
        (((CraftPlayer)player).getHandle()).b.a(packet1);
    }

    public void equipIcon(org.bukkit.inventory.ItemStack bukkiticon) {
        ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(box.ae(), Collections.singletonList(new Pair(EnumItemSlot.f, icon)));
        (((CraftPlayer)owner).getHandle()).b.a((Packet)packet);
    }

    private void equipIcon(EntityArmorStand armorStand, org.bukkit.inventory.ItemStack bukkiticon) {
        ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(armorStand.ae(), Collections.singletonList(new Pair(EnumItemSlot.f, icon)));
        (((CraftPlayer)owner).getHandle()).b.a((Packet)packet);
    }

    public boolean open() {
        Main.handler.removeButtonsFromPlayer(owner, ButtonType.REWARD);
        PlayerOpenLCEvent event = new PlayerOpenLCEvent(owner, this);
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled())
            return false;
        return true;
    }

    public void remove() {
        if (box != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { box.ae() });
            (((CraftPlayer)owner).getHandle()).b.a(packet);
            boxlists.remove(Integer.valueOf(id));
            boxPlayerlists.remove(owner);
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean bool) {
        clickable = bool;
    }

    public Player getOwner() {
        return owner;
    }

    public EntityArmorStand getBox() {
        return box;
    }

    public Location getLoc() {
        return loc;
    }

    public org.bukkit.inventory.ItemStack getIcon() {
        return icon;
    }

    public TConfigManager getConfig() {
        return config;
    }

    public String getBoxId() {
        return boxid;
    }

    public Location getPlayerInitialLoc() {
        return initialLocation;
    }

    public Map<Player, Pig> getVehiclesList() {
        return vehicles;
    }

    public void removeVehicle(Player p) {
        if (vehicles.get(p) != null) {
            ((Pig)vehicles.get(p)).remove();
            vehicles.remove(p);
        }
    }

    public TBox getInstance() {
        return instance;
    }

    public boolean isClickToOpen() {
        return clickToOpen;
    }

    public void setOpeningState(Boolean state) {
        isOpening = state;
    }

    public boolean isOpening() {
        return isOpening;
    }

    public Location getPlayerLocation() {
        return playerLocation;
    }

    public List<Location> getRewardsLocation() {
        return new ArrayList<>(rewardsLocation);
    }
}

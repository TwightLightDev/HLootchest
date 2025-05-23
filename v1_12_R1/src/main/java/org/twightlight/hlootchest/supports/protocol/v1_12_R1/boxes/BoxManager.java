package org.twightlight.hlootchest.supports.protocol.v1_12_R1.boxes;

import org.twightlight.libs.xseries.XPotion;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPig;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.lootchest.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.player.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.Main;
import org.twightlight.hlootchest.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager implements TBox {

    private Player owner;
    int id;
    private EntityArmorStand box;
    private ArmorStand boxreferrence;
    private boolean clickable = true;
    private Location loc;
    private ItemStack icon;
    private TConfigManager config;
    private String boxid;
    private TBox instance;
    private boolean clickToOpen;
    private Location playerLocation;
    private List<Location> rewardsLocation = new ArrayList<>();

    private static final Map<Player, Pig> vehicles = new HashMap<>();
    public static final ConcurrentHashMap<Integer, TBox> boxlists = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, TBox> boxPlayerlists = new ConcurrentHashMap<>();
    private boolean isOpening;

    public BoxManager(Location location, Player player, ItemStack icon, TConfigManager config, String boxid) {
        Main.api.getSessionUtil().getSessionFromPlayer(player).setBox(this);

        this.owner = player;


        Location Plocation = Utility.stringToLocation(config.getString(boxid + ".settings.player-location"));

        playerLocation = Plocation;

        if (vehicles.get(owner) == null) {

            Pig vehicle = (Pig) Plocation.getWorld().spawnEntity(Plocation.clone().add(0, -0.3, 0), EntityType.PIG);
            vehicle.addPotionEffect(new PotionEffect(XPotion.INVISIBILITY.getPotionEffectType(), Integer.MAX_VALUE, 1, false, false));

            vehicle.setCustomName("LootchestVehicle");
            vehicle.setCustomNameVisible(false);
            vehicle.setSilent(true);
            vehicle.setCollidable(false);
            vehicle.setGravity(false);


            EntityPig entityPig = ((CraftPig) vehicle).getHandle();
            NBTTagCompound tag = new NBTTagCompound();

            entityPig.c(tag);

            tag.setInt("NoAI", 1);

            entityPig.f(tag);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(Plocation);
                    vehicle.addPassenger(player);

                }
            }.runTaskLater(Main.handler.plugin, 2L);
            vehicles.put(owner, vehicle);
        }


        this.box = createArmorStand(location, "", false);

        this.boxreferrence = (ArmorStand) box.getBukkitEntity();

        this.clickToOpen = config.getBoolean(boxid + ".settings.click-to-open");

        this.instance = this;

        this.id = box.getId();

        this.loc = location;

        this.icon = icon;

        this.config = config;

        this.boxid = boxid;

        Main.nmsUtil.rotate(box, config, boxid + ".settings");

        boxlists.put(id, this);
        boxPlayerlists.put(owner, this);
        sendSpawnPacket(owner, box);

        equipIcon(box, icon);

        if (config.getList(boxid + ".rewards-location") != null) {
            for (String reward : config.getList(boxid + ".rewards-location")) {
                rewardsLocation.add(Utility.stringToLocation(reward));
            }
        }

        LCSpawnEvent event = new LCSpawnEvent(owner, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.api.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return armorStand;
    }

    public void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void equipIcon(ItemStack bukkiticon) {
        net.minecraft.server.v1_12_R1.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                box.getId(),
                EnumItemSlot.HEAD,
                icon
        );
        ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
    }

    private void equipIcon(EntityArmorStand armorStand, ItemStack bukkiticon) {
        net.minecraft.server.v1_12_R1.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                armorStand.getId(),
                EnumItemSlot.HEAD,
                icon
        );
        ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
    }

    public boolean open() {

        Main.handler.removeButtonsFromPlayer(owner, ButtonType.REWARD);

        PlayerOpenLCEvent event = new PlayerOpenLCEvent(owner, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }
        return true;
    }

    public void remove() {
        if (box != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(box.getId());
            ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
            boxlists.remove(id);
            boxPlayerlists.remove(owner);
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean bool) {
        this.clickable = bool;
    }

    public Player getOwner() {
        return owner;
    }

    public EntityArmorStand getBox() {
        return box;
    }

    public ArmorStand getBoxReferrence() {
        return boxreferrence;
    }

    public Location getLoc() {
        return loc;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public TConfigManager getConfig() { return config; }

    public String getBoxId() { return boxid; }


    public Map<Player, Pig> getVehiclesList() {
        return vehicles;
    }

    public void removeVehicle(Player p) {
        if (vehicles.get(p) != null) {
            vehicles.get(p).remove();
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

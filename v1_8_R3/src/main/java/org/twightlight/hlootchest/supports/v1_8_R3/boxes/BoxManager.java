package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager implements TBox {

    private Player owner;
    int id;
    private EntityArmorStand box;
    private boolean clickable = true;
    private Location loc;
    private ItemStack icon;
    private TConfigManager config;
    private String boxid;
    private Pig vehicle;

    private static Location playerInitialLoc = null;

    public static ConcurrentHashMap<Integer, TBox> boxlists = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Player, TBox> boxPlayerlists = new ConcurrentHashMap<>();

    public BoxManager(Player player, ItemStack icon, TConfigManager config, String boxid) {
        this.owner = player;

        if (playerInitialLoc == null) {
            playerInitialLoc = player.getLocation();
        }

        Location location = v1_8_R3.stringToLocation(config.getString(boxid + ".settings.location"));

        this.box = createArmorStand(location, config.getString(boxid+".settings.name"), config.getBoolean(boxid+".settings.enable-name"));

        this.id = box.getId();

        this.loc = location;

        this.icon = icon;

        this.config = config;

        this.boxid = boxid;

        v1_8_R3.rotate(box, config, boxid+".settings");

        boxlists.put(id, this);
        boxPlayerlists.put(player, this);
        sendSpawnPacket(player, box);
        equipIcon(box, icon);

        Location Plocation = v1_8_R3.stringToLocation(config.getString(boxid + ".settings.player-location"));
        owner.teleport(Plocation);

        vehicle = (Pig) Plocation.add(0, -1, 0).getWorld().spawnEntity(location, EntityType.PIG);

        Entity entity = ((CraftWorld) location.getWorld()).getHandle().getEntity(vehicle.getUniqueId());
        if (entity instanceof EntityPig) {
            NBTTagCompound nbt = new NBTTagCompound();
            entity.c(nbt);
            nbt.setByte("NoAI", (byte) 1);
            entity.f(nbt);
        }

        vehicle.setCustomName("LootchestPig");
        vehicle.setCustomNameVisible(false);
        vehicle.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

        vehicle.setPassenger(owner);
    }

    public EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
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

    public void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void equipIcon(EntityArmorStand armorStand, ItemStack bukkiticon) {
        net.minecraft.server.v1_8_R3.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                armorStand.getId(),
                4,
                icon
        );
        ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(packet);
    }

    public void open() {
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

    public Location getLoc() {
        return loc;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public TConfigManager getConfig() { return config; }

    public String getBoxId() { return boxid; }

    public Location getPlayerInitialLoc() {
        return playerInitialLoc;
    }

    public void resetPlayerInitialLoc() {
        playerInitialLoc = null;
    }

    public Pig getVehicle() {
        return vehicle;
    }
}

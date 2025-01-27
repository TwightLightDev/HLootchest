package org.twightlight.hlootchest.supports.v1_8_R3.boxes;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.twightlight.hlootchest.api.enums.BoxType;

import java.util.concurrent.ConcurrentHashMap;

public class BoxManager {

    private Player owner;
    int id;
    private BoxType boxType = BoxType.OTHERS;
    private EntityArmorStand box;
    private boolean clickable = true;
    private Location loc;
    private ItemStack icon;

    public static ConcurrentHashMap<Integer, BoxManager> boxlists = new ConcurrentHashMap<>();

    public BoxManager(Player player, Location location, ItemStack icon, BoxType boxType) {
        this.owner = player;

        this.box = createArmorStand(location);

        this.id = box.getId();

        this.loc = location;

        this.icon = icon;

        this.boxType = boxType;

        boxlists.put(id, this);
        sendSpawnPacket(player, box);
        equipIcon(box, icon);
    }

    public EntityArmorStand createArmorStand(Location location) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(false);
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

    public BoxType getBoxType() {
        return boxType;
    }

}

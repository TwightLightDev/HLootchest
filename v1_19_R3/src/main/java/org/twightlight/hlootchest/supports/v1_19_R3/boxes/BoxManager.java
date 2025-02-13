// Decompiled with: CFR 0.152
// Class Version: 8
package org.twightlight.hlootchest.supports.v1_19_R3.boxes;

import com.cryptomorin.xseries.XPotion;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.LCSpawnEvent;
import org.twightlight.hlootchest.api.events.PlayerOpenLCEvent;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager
        implements TBox {
    private Player owner;
    int id;
    private EntityArmorStand box;
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
        this.id = this.box.af();
        this.loc = location;
        this.icon = icon;
        this.config = config;
        this.boxid = boxid;
        Main.rotate(this.box, config, boxid + ".settings");
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.box.af(), this.box.aj().c());
        ((CraftPlayer)this.owner).getHandle().b.a(metadataPacket);
        boxlists.put(this.id, this);
        boxPlayerlists.put(this.owner, this);
        this.sendSpawnPacket(this.owner, this.box);
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
        }
        LCSpawnEvent lCSpawnEvent = new LCSpawnEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent(lCSpawnEvent);
    }

    public EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand((World)nmsWorld, location.getX(), location.getY(), location.getZ());
        armorStand.n(isNameEnable);
        armorStand.b(IChatBaseComponent.a((String)Main.p(this.owner, ChatColor.translateAlternateColorCodes('&', name))));
        armorStand.e(true);
        armorStand.j(true);
        armorStand.f(location.getYaw());
        armorStand.e(location.getPitch());
        return armorStand;
    }

    public void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity((Entity)armorStand);
        ((CraftPlayer)player).getHandle().b.a((Packet)packet);
        armorStand.aj().b(EntityArmorStand.bB, Byte.valueOf((byte)32));
        PacketPlayOutEntityMetadata packet1 = new PacketPlayOutEntityMetadata(armorStand.af(), armorStand.aj().c());
        ((CraftPlayer)player).getHandle().b.a((Packet)packet1);
    }

    @Override
    public void equipIcon(ItemStack bukkiticon) {
        net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy((ItemStack)bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.box.af(), Collections.singletonList(new Pair((Object)EnumItemSlot.f, (Object)icon)));
        ((CraftPlayer)this.owner).getHandle().b.a((Packet)packet);
    }

    private void equipIcon(EntityArmorStand armorStand, ItemStack bukkiticon) {
        net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy((ItemStack)bukkiticon);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(armorStand.af(), Collections.singletonList(new Pair((Object)EnumItemSlot.f, (Object)icon)));
        ((CraftPlayer)this.owner).getHandle().b.a((Packet)packet);
    }

    @Override
    public boolean open() {
        Main.handler.removeButtonsFromPlayer(this.owner, ButtonType.REWARD);
        PlayerOpenLCEvent event = new PlayerOpenLCEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    @Override
    public void remove() {
        if (this.box != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]{this.box.af()});
            ((CraftPlayer)this.owner).getHandle().b.a((Packet)packet);
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

    public EntityArmorStand getBox() {
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

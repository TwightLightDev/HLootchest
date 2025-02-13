package org.twightlight.hlootchest.supports.v1_19_R3.buttons;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.ButtonSpawnEvent;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.v1_19_R3.utilities.Animations;

public class Button implements TButton {
    private final int id;

    private final Player owner;

    private final EntityArmorStand armorstand;

    private boolean isMoved = false;

    private final BukkitTask task;

    private boolean clickable = false;

    private boolean moveable = true;

    private ButtonType type;

    private List<String> actions;

    private ButtonSound sound;

    private boolean isHiding = false;

    private ItemStack icon = null;

    private TConfigManager config;

    private String pathToButton;

    private String nameVisibleMode = "always";

    private boolean removed = false;

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    public static final Map<EntityArmorStand, String> linkedStandsSettings = new HashMap<>();

    public static final Map<TButton, List<EntityArmorStand>> linkedStands = new HashMap<>();

    public static final Map<EntityArmorStand, ItemStack> linkedStandsIcon = new HashMap<>();

    public Button(final Location location, ButtonType type, Player player, ItemStack icon, final String path, final TConfigManager config) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;
        ButtonSpawnEvent event = new ButtonSpawnEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent((Event)event);
        this.actions = (config.getList(path + ".actions") != null) ? config.getList(path + ".actions") : new ArrayList<>();
        if (config.getYml().contains(path + ".click-sound"))
            this.sound = new ButtonSound(XSound.valueOf(config.getString(path + ".click-sound.sound")).parseSound(), (float)config.getDouble(path + ".click-sound.yaw"), (float)config.getDouble(path + ".click-sound.pitch"));
        boolean enableName = config.getYml().contains(path + ".name") ? config.getBoolean(path + ".name.enable") : false;
        if (config.getYml().contains(path + ".name.visible-mode") && enableName) {
            String mode = config.getString(path + ".name.visible-mode");
            if (mode.equals("hover"))
                enableName = false;
            this.nameVisibleMode = mode;
        }
        final EntityArmorStand armorStand = createArmorStand(location, (config.getString(path + ".name.display-name") != null) ? config.getString(path + ".name.display-name") : "", enableName);
        this.id = armorStand.af();
        this.armorstand = armorStand;
        Main.rotate(this.armorstand, config, path);
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.armorstand.af(), this.armorstand.aj().c());
        (((CraftPlayer)this.owner).getHandle()).b.a((Packet)metadataPacket);
        buttonIdMap.put(Integer.valueOf(this.id), this);
        playerButtonMap.computeIfAbsent(player, k -> new ArrayList()).add(this);
        linkedStands.put(this, new ArrayList<>());
        this.type = type;
        sendSpawnPacket(player, armorStand);
        if (config.getYml().contains(path + ".name.refresh-interval")) {
            int interval = config.getInt(path + ".name.refresh-interval");
            (new BukkitRunnable() {
                public void run() {
                    if (!Button.this.owner.isOnline() || Button.this.removed) {
                        cancel();
                        return;
                    }
                    PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.af(), armorStand.aj().c());
                    Button.this.armorstand.b(IChatBaseComponent.a(Main.p(Button.this.owner, ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name.display-name")))));
                    (((CraftPlayer)Button.this.owner).getHandle()).b.a((Packet)packet);
                }
            }).runTaskTimer(Main.handler.plugin, 0L, interval);
        }
        boolean rotateOnSpawn = config.getYml().contains(path + ".rotate-on-spawn") ? config.getBoolean(path + ".rotate-on-spawn.enable") : false;
        if (rotateOnSpawn) {
            final float angle = (float)(location.clone().getYaw() - config.getDouble(path + ".rotate-on-spawn.final-yaw"));
            final boolean reverse = config.getBoolean(path + ".rotate-on-spawn.reverse");
            (new BukkitRunnable() {
                int i = 0;

                public void run() {
                    int multiply;
                    if (!Button.this.owner.isOnline() || this.i >= 1) {
                        cancel();
                        return;
                    }
                    this.i++;
                    if (reverse) {
                        multiply = 1;
                    } else {
                        multiply = -1;
                    }
                    Animations.Spinning(Button.this.owner, Button.this.armorstand, location.clone().getYaw() - angle * this.i * multiply);
                }
            }).runTaskTimer(Main.handler.plugin, 2L, 1L);
        }
        boolean isHoldingIcon = config.getYml().contains(path + ".holding-icon") ? config.getBoolean(this.pathToButton + ".holding-icon") : true;
        if (isHoldingIcon) {
            equipIcon(armorStand, icon);
            this.icon = icon;
        }
        unmovablePeriod(150);
        if (config.getYml().getConfigurationSection(path + ".children") != null) {
            Set<String> linkeds = config.getYml().getConfigurationSection(path + ".children").getKeys(false);
            for (String childname : linkeds) {
                final String newpath = path + ".children." + childname;
                Location childlocation = null;
                if (config.getString(newpath + ".location") != null) {
                    childlocation = Main.handler.stringToLocation(config.getString(newpath + ".location"));
                } else if (config.getString(newpath + ".location-offset") != null) {
                    String[] offsetXYZ = config.getString(newpath + ".location-offset").split(",");
                    double yaw = this.armorstand.getBukkitYaw();
                    Vector vector = this.armorstand.getBukkitEntity().getLocation().getDirection().normalize();
                    Expression exp = (new ExpressionBuilder(offsetXYZ[0])).variables(new String[] { "yaw", "VectorX" }).build().setVariable("yaw", Math.toRadians(yaw)).setVariable("VectorX", vector.getX());
                    Expression exp1 = (new ExpressionBuilder(offsetXYZ[1])).variables(new String[] { "yaw", "VectorY" }).build().setVariable("yaw", Math.toRadians(yaw)).setVariable("VectorY", vector.getY());
                    Expression exp2 = (new ExpressionBuilder(offsetXYZ[2])).variables(new String[] { "yaw", "VectorZ" }).build().setVariable("yaw", Math.toRadians(yaw)).setVariable("VectorZ", vector.getZ());
                    childlocation = location.clone().add(exp.evaluate(), exp1.evaluate(), exp2.evaluate());
                }
                if (childlocation != null) {
                    boolean childEnableName = config.getYml().contains(newpath + ".name") ? config.getBoolean(newpath + ".name.enable") : false;
                    String childNameVisibleMode = "always";
                    if (config.getYml().contains(newpath + ".name.visible-mode") && childEnableName) {
                        String mode = config.getString(newpath + ".name.visible-mode");
                        if (mode.equals("hover"))
                            childEnableName = false;
                        childNameVisibleMode = mode;
                    }
                    final EntityArmorStand child = createArmorStand(childlocation, (config.getString(newpath + ".name.display-name") != null) ? config.getString(newpath + ".name.display-name") : "", childEnableName);
                    Main.rotate(child, config, newpath);
                    PacketPlayOutEntityMetadata metadataPacket1 = new PacketPlayOutEntityMetadata(child.af(), child.aj().c());
                    (((CraftPlayer)this.owner).getHandle()).b.a((Packet)metadataPacket1);
                    linkedStandsSettings.put(child, childNameVisibleMode);
                    ((List<EntityArmorStand>)linkedStands.get(this)).add(child);
                    sendSpawnPacket(player, child);
                    if (config.getYml().contains(newpath + ".name.refresh-interval")) {
                        int interval = config.getInt(newpath + ".name.refresh-interval");
                        (new BukkitRunnable() {
                            public void run() {
                                if (!Button.this.owner.isOnline() || Button.this.removed) {
                                    cancel();
                                    return;
                                }
                                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(child.af(), child.aj().c());
                                child.b(IChatBaseComponent.a(Main.p(Button.this.owner, ChatColor.translateAlternateColorCodes('&', config.getString(newpath + ".name.display-name")))));
                                (((CraftPlayer)Button.this.owner).getHandle()).b.a((Packet)packet);
                            }
                        }).runTaskTimer(Main.handler.plugin, 0L, interval);
                    }
                    if (config.getYml().contains(newpath + ".icon")) {
                        ItemStack childicon = Main.handler.createItem(XMaterial.valueOf(config.getString(newpath + ".icon.material")).parseMaterial(), config.getString(newpath + ".icon.head_value"), config.getInt(newpath + ".icon.data"), "", new ArrayList(), false);
                        equipIcon(child, childicon);
                        linkedStandsIcon.put(child, childicon);
                    }
                }
            }
        }
        boolean moveForward = config.getYml().contains(path + ".move-forward") ? config.getBoolean(path + ".move-forward") : true;
        if (moveForward) {
            ButtonSound hoverSound;
            if (config.getYml().contains(path + ".hover-sound")) {
                hoverSound = new ButtonSound(XSound.valueOf(config.getString(path + ".hover-sound.sound")).parseSound(), (float)config.getDouble(path + ".hover-sound.yaw"), (float)config.getDouble(path + ".hover-sound.pitch"));
            } else {
                hoverSound = null;
            }
            this.task = Bukkit.getScheduler().runTaskTimer(Main.handler.plugin, () -> {
                if (this.owner == null || this.armorstand == null || !this.owner.isOnline()) {
                    cancelTask();
                    return;
                }
                if (linkedStands == null || !linkedStands.containsKey(this)) {
                    cancelTask();
                    return;
                }
                if (this.owner.getLocation().distance(this.armorstand.getBukkitEntity().getLocation()) > 5.0D || this.isHiding) {
                    if (this.isMoved) {
                        moveBackward();
                        if (this.nameVisibleMode.equals("hover"))
                            sendNameVisibilityPacket(this.owner, this.armorstand, false);
                        for (EntityArmorStand stand : linkedStands.get(this)) {
                            if (((String)linkedStandsSettings.get(stand)).equals("hover"))
                                sendNameVisibilityPacket(this.owner, stand, false);
                        }
                        unmovablePeriod(500);
                        this.isMoved = false;
                    }
                    return;
                }
                Location playerEye = this.owner.getEyeLocation();
                Vector playerDirection = playerEye.getDirection().normalize();
                Vector playerPosition = playerEye.toVector();
                Vector armorStandPosition = new Vector(this.armorstand.dl(), this.armorstand.dn() + 1.6D, this.armorstand.dr());
                Vector toArmorStand = armorStandPosition.subtract(playerPosition).normalize();
                double dotProduct = playerDirection.dot(toArmorStand);
                if (dotProduct > 0.98D) {
                    if (!this.isMoved && this.moveable) {
                        moveForward();
                        if (hoverSound != null)
                            Main.handler.playSound(getOwner(), getOwner().getLocation(), hoverSound.getSoundString(), hoverSound.getYaw(), hoverSound.getPitch());
                        if (this.nameVisibleMode.equals("hover"))
                            sendNameVisibilityPacket(this.owner, this.armorstand, true);
                        for (EntityArmorStand stand : linkedStands.get(this)) {
                            if (((String)linkedStandsSettings.get(stand)).equals("hover"))
                                sendNameVisibilityPacket(this.owner, stand, true);
                        }
                        unmovablePeriod(500);
                        this.isMoved = true;
                    }
                } else if (this.isMoved && this.moveable) {
                    moveBackward();
                    if (this.nameVisibleMode.equals("hover"))
                        sendNameVisibilityPacket(this.owner, this.armorstand, false);
                    for (EntityArmorStand stand : linkedStands.get(this)) {
                        if (((String)linkedStandsSettings.get(stand)).equals("hover"))
                            sendNameVisibilityPacket(this.owner, stand, false);
                    }
                    unmovablePeriod(500);
                    this.isMoved = false;
                }
            }, 0L, 5L);
        } else {
            this.task = null;
        }
    }

    private EntityArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand((World)nmsWorld, location.getX(), location.getY(), location.getZ());
        armorStand.n(isNameEnable);
        armorStand.b(IChatBaseComponent.a(Main.p(this.owner, ChatColor.translateAlternateColorCodes('&', name))));
        armorStand.e(true);
        armorStand.j(true);
        armorStand.f(location.getYaw());
        armorStand.e(location.getPitch());
        return armorStand;
    }

    private void sendSpawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity((Entity)armorStand);
        (((CraftPlayer)player).getHandle()).b.a((Packet)packet);
        armorStand.aj().b(EntityArmorStand.bB, Byte.valueOf((byte)32));
        PacketPlayOutEntityMetadata packet1 = new PacketPlayOutEntityMetadata(armorStand.af(), armorStand.aj().c());
        (((CraftPlayer)player).getHandle()).b.a((Packet)packet1);
    }

    private void sendDespawnPacket(Player player, EntityArmorStand armorStand) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { armorStand.af() });
        (((CraftPlayer)player).getHandle()).b.a((Packet)packet);
    }

    private void unmovablePeriod(int time) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.moveable = false;
        scheduler.schedule(() -> {
            this.moveable = true;
            scheduler.shutdown();
        }, time, TimeUnit.MILLISECONDS);
    }

    public void equipIcon(ItemStack bukkiticon) {
        if (bukkiticon != null) {
            net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            EnumItemSlot slot = EnumItemSlot.f;
            if (this.type == ButtonType.REWARD)
                slot = EnumItemSlot.a;
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.armorstand.af(), Collections.singletonList(new Pair(slot, icon)));
            (((CraftPlayer)this.owner).getHandle()).b.a((Packet)packet);
        }
    }

    private void equipIcon(EntityArmorStand armorStand, ItemStack bukkiticon) {
        if (bukkiticon != null) {
            net.minecraft.world.item.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            EnumItemSlot slot = EnumItemSlot.f;
            if (this.type == ButtonType.REWARD)
                slot = EnumItemSlot.a;
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.armorstand.af(), Collections.singletonList(new Pair(slot, icon)));
            (((CraftPlayer)this.owner).getHandle()).b.a((Packet)packet);
        }
    }

    public void hide(boolean isHiding) {
        this.isHiding = isHiding;
        if (isHiding) {
            sendDespawnPacket(this.owner, this.armorstand);
            for (EntityArmorStand stand : linkedStands.get(this))
                sendDespawnPacket(this.owner, stand);
        } else {
            sendSpawnPacket(this.owner, this.armorstand);
            equipIcon(this.armorstand, this.icon);
            for (EntityArmorStand stand : linkedStands.get(this)) {
                sendSpawnPacket(this.owner, stand);
                if (linkedStandsIcon.get(stand) != null)
                    equipIcon(stand, linkedStandsIcon.get(stand));
            }
        }
    }

    public void remove() {
        if (this.armorstand != null) {
            this.removed = true;
            cancelTask();
            sendDespawnPacket(this.owner, this.armorstand);
            for (EntityArmorStand stand : linkedStands.get(this)) {
                sendDespawnPacket(this.owner, stand);
                linkedStandsIcon.remove(stand);
            }
            buttonIdMap.remove(Integer.valueOf(this.id));
            linkedStands.remove(this);
            List<TButton> buttons = playerButtonMap.get(this.owner);
            if (buttons != null) {
                buttons.remove(this);
                playerButtonMap.put(this.owner, buttons);
            }
        }
    }

    public void moveForward() {
        this.clickable = true;
        Animations.MoveForward(this.owner, this.armorstand, 0.5F);
        for (EntityArmorStand stand : linkedStands.get(this))
            Animations.MoveForward(this.owner, stand, 0.5F);
    }

    public void moveBackward() {
        this.clickable = false;
        Animations.MoveBackward(this.owner, this.armorstand, 0.5F);
        for (EntityArmorStand stand : linkedStands.get(this))
            Animations.MoveBackward(this.owner, stand, 0.5F);
    }

    public void sendNameVisibilityPacket(Player player, EntityArmorStand armorStand, boolean visible) {
        armorStand.n(visible);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.af(), armorStand.aj().c());
        (((CraftPlayer)player).getHandle()).b.a((Packet)packet);
    }

    public int getCustomId() {
        return this.id;
    }

    public Player getOwner() {
        return this.owner;
    }

    public EntityArmorStand getEntity() {
        return this.armorstand;
    }

    public void cancelTask() {
        if (this.task != null)
            this.task.cancel();
    }

    public boolean isMoved() {
        return this.isMoved;
    }

    public boolean isClickable() {
        return this.clickable;
    }

    public void setClickable(boolean bool) {
        this.clickable = bool;
    }

    public ButtonType getType() {
        return this.type;
    }

    public List<String> getActions() {
        return this.actions;
    }

    public ButtonSound getSound() {
        return this.sound;
    }

    public boolean isHiding() {
        return this.isHiding;
    }

    public static class ButtonSound {
        Sound sound;

        float yaw;

        float pitch;

        public ButtonSound(Sound sound, float yaw, float pitch) {
            this.sound = sound;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Sound getSound() {
            return this.sound;
        }

        public String getSoundString() {
            return XSound.matchXSound(this.sound).name();
        }

        public float getYaw() {
            return this.yaw;
        }

        public float getPitch() {
            return this.pitch;
        }
    }
}
package org.twightlight.hlootchest.supports.v1_8_R3.buttons;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.minecraft.server.v1_8_R3.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.events.lootchest.ButtonSpawnEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.objects.ButtonSound;
import org.twightlight.hlootchest.objects.Icon;
import org.twightlight.hlootchest.supports.v1_8_R3.Main;
import org.twightlight.hlootchest.supports.v1_8_R3.utilities.Animations;

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
    private boolean clickable = false;
    private boolean moveable = true;
    private ButtonType type;
    private List<String> actions;
    private ButtonSound sound;
    private boolean isHiding = false;
    private TIcon icon = null;
    private TConfigManager config;
    private String pathToButton;
    private boolean dynamicName;
    private boolean dynamicIcon;

    private String nameVisibleMode = "always";
    private boolean removed = false;

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    public static final Map<EntityArmorStand, List<String>> linkedStandsSettings = new HashMap<>();
    public static final Map<TButton, List<EntityArmorStand>> linkedStands = new HashMap<>();
    public static final Map<EntityArmorStand, TIcon> linkedStandsIcon = new HashMap<>();

    public Button(Location location, ButtonType type, Player player, ItemStack icon, String path, TConfigManager config) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;

        ButtonSpawnEvent event = new ButtonSpawnEvent(owner, this);
        Bukkit.getPluginManager().callEvent(event);

        this.actions = (config.getList(path+".actions") != null) ? config.getList(path+".actions") : new ArrayList<>();
        if (config.getYml().contains(path+".click-sound")) {
            this.sound = new ButtonSound(XSound.of(config.getString(path + ".click-sound.sound")).get().get(), (float) config.getDouble(path + ".click-sound.yaw"), (float) config.getDouble(path + ".click-sound.pitch"));
        }

        boolean enableName = (config.getYml().contains(path+".name")) ? config.getBoolean(path+".name.enable") : false;
        if (config.getYml().contains(path+".name.visible-mode") && enableName) {
            String mode = config.getString(path+".name.visible-mode");
            if (mode.equals("hover")) {
                enableName = false;
            }
            nameVisibleMode = mode;
        }


        dynamicName = (config.getYml().contains(path + ".name.dynamic")) ? config.getBoolean(path + ".name.dynamic") : false;
        String name = "";
        if (!dynamicName) {
            name = (config.getString(path + ".name.display-name") != null) ? config.getString(path + ".name.display-name") : "";
        } else {
            name = (config.getList(path + ".name.display-name") != null) ? config.getList(path + ".name.display-name").get(0) : "";
        }
        boolean isSmall = config.getBoolean(path + ".small", false);
        final EntityArmorStand armorStand = createArmorStand(location, name, isSmall, enableName);
        this.id = armorStand.getId();

        this.armorstand = armorStand;
        Main.rotate(armorstand, config, path);

        buttonIdMap.put(this.id, this);
        playerButtonMap.computeIfAbsent(player, k -> new ArrayList<>()).add(this);
        linkedStands.put(this, new ArrayList<>());

        this.type = type;

        sendSpawnPacket(player, armorStand);

        if (config.getYml().contains(path + ".name.refresh-interval")) {
            int interval = config.getInt(path + ".name.refresh-interval");
            List<String> names = config.getList(path + ".name.display-name");
            (new BukkitRunnable() {
                int i = 1;
                public void run() {
                    if (!owner.isOnline() || removed) {
                        cancel();
                        return;
                    }
                    if (!dynamicName) {
                        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);
                        armorstand.setCustomName(Main.p(owner, config.getString(path + ".name.display-name")));
                        (((CraftPlayer)owner).getHandle()).playerConnection.sendPacket(packet);
                    } else {
                        if (i >= names.size()) {
                            i = 0;
                        }
                        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);
                        armorstand.setCustomName(Main.p(owner, names.get(i)));
                        (((CraftPlayer)owner).getHandle()).playerConnection.sendPacket(packet);
                        i ++;
                    }
                }
            }).runTaskTimer(Main.handler.plugin, 0L, interval);
        }

        boolean rotateOnSpawn = (config.getYml().contains(path+".rotate-on-spawn")) ? config.getBoolean(path+".rotate-on-spawn.enable") : false;
        if (rotateOnSpawn) {
            float angle = (float) (location.clone().getYaw() - config.getDouble(path+".rotate-on-spawn.final-yaw"));
            boolean reverse = config.getBoolean(path+".rotate-on-spawn.reverse");
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (!owner.isOnline() || i >= 1) {
                        this.cancel();
                        return;
                    }

                    i ++;
                    int multiply;
                    if (reverse) {
                        multiply = 1;
                    } else {
                        multiply = -1;
                    }
                    Animations.Spinning(owner, armorstand, location.clone().getYaw() - ((angle) * i * multiply));
                }
            }.runTaskTimer(Main.handler.plugin, 2L, 1L);
        }

        boolean isHoldingIcon = (config.getYml().contains(path + ".holding-icon")) ? config.getBoolean(pathToButton + ".holding-icon") : true;

        dynamicIcon = (config.getYml().contains(path + ".icon.dynamic")) ? config.getBoolean(path + ".icon.dynamic") : false;
        if (isHoldingIcon && !dynamicIcon) {
            ItemSlot slot = ItemSlot.valueOf(config.getString(path + ".icon.slot", "HEAD"));
            TIcon ticon = new Icon(icon, slot);
            equipIcon(armorStand, ticon);
            this.icon = ticon;
        }
        if (isHoldingIcon && dynamicIcon && config.getYml().contains(path + ".icon.refresh-interval")) {
            int interval = config.getInt(path + ".icon.refresh-interval");
            List<String> iconPaths = new ArrayList<>(config.getYml().getConfigurationSection(path + ".icon.dynamic-icons").getKeys(false));
            List<TIcon> icons = new ArrayList<>();
            for (String iconPath : iconPaths) {
                String thisIconPath = path + ".icon.dynamic-icons." + iconPath;
                String iconMaterial = config.getString(thisIconPath + ".material");
                String iconHeadValue = config.getString(thisIconPath + ".head_value");
                int iconData = (config.getYml().contains(thisIconPath + ".data")) ? config.getInt(thisIconPath + ".data") : 0;
                boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
                ItemSlot slot = ItemSlot.valueOf(config.getString(thisIconPath + ".slot", "HEAD"));
                ItemStack thisIcon = Main.handler.createItem(XMaterial.valueOf(iconMaterial).get(), iconHeadValue, iconData, "", new ArrayList<>(), isGlowing);
                icons.add(new Icon(thisIcon, slot));
            }
            (new BukkitRunnable() {
                int i = 1;
                public void run() {
                    if (!Button.this.owner.isOnline() || Button.this.removed) {
                        cancel();
                        return;
                    }
                    if (i >= icons.size()) {
                        i = 0;
                    }
                    if (!isHiding) {
                        equipIcon(armorStand, icons.get(i));
                        setIcon(icons.get(i));
                    }
                    i ++;

                }
            }).runTaskTimer(Main.handler.plugin, 0L, interval);
        }
        unmovablePeriod(250);

        if (config.getYml().getConfigurationSection(path + ".children") != null) {

            Set<String> linkeds = config.getYml().getConfigurationSection(path + ".children").getKeys(false);

            for (String childname : linkeds) {
                String newpath = path + ".children" + "." + childname;

                Location childlocation = null;

                if (Main.api.getPlayerUtil().checkConditions(player, config, newpath + ".spawn-requirements")) {
                    if (config.getString(newpath + ".location") != null) {
                        childlocation = Main.handler.stringToLocation(config.getString(newpath + ".location"));
                    } else if (config.getString(newpath + ".location-offset") != null) {
                        String[] offsetXYZ = config.getString(newpath + ".location-offset").split(",");
                        double yaw = this.armorstand.yaw;
                        Vector vector = this.armorstand.getBukkitEntity().getLocation().getDirection().normalize();
                        if (offsetXYZ.length == 3) {
                            Expression exp = (new ExpressionBuilder(offsetXYZ[0]))
                                    .variables(new String[]{"yaw", "VectorX"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorX", vector.getX());
                            Expression exp1 = (new ExpressionBuilder(offsetXYZ[1]))
                                    .variables(new String[]{"yaw", "VectorY"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorY", vector.getY());
                            Expression exp2 = (new ExpressionBuilder(offsetXYZ[2]))
                                    .variables(new String[]{"yaw", "VectorZ"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorZ", vector.getZ());
                            childlocation = location.clone().add(exp.evaluate(), exp1.evaluate(), exp2.evaluate());

                        } else if (offsetXYZ.length == 5) {
                            Expression exp = (new ExpressionBuilder(offsetXYZ[0]))
                                    .variables(new String[]{"yaw", "VectorX"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorX", vector.getX());
                            Expression exp1 = (new ExpressionBuilder(offsetXYZ[1]))
                                    .variables(new String[]{"yaw", "VectorY"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorY", vector.getY());
                            Expression exp2 = (new ExpressionBuilder(offsetXYZ[2]))
                                    .variables(new String[]{"yaw", "VectorZ"})
                                    .build()
                                    .setVariable("yaw", Math.toRadians(yaw))
                                    .setVariable("VectorZ", vector.getZ());
                            Expression exp3 = (new ExpressionBuilder(offsetXYZ[3])).build();
                            Expression exp4 = (new ExpressionBuilder(offsetXYZ[4])).build();
                            Location locClone = location.clone();
                            locClone.setYaw(location.getYaw() + (float) exp3.evaluate());
                            locClone.setPitch(location.getPitch() + (float) exp4.evaluate());
                            childlocation = locClone.add(exp.evaluate(), exp1.evaluate(), exp2.evaluate());
                        }
                    }

                    if (childlocation != null) {
                        boolean childEnableName = (config.getYml().contains(newpath + ".name")) ? config.getBoolean(newpath + ".name.enable") : false;
                        String childNameVisibleMode = "always";
                        if (config.getYml().contains(newpath + ".name.visible-mode") && childEnableName) {
                            String mode = config.getString(newpath + ".name.visible-mode");
                            if (mode.equals("hover")) {
                                childEnableName = false;
                            }
                            childNameVisibleMode = mode;
                        }

                        boolean isChildNI = (config.getYml().contains(newpath + ".name.dynamic")) ? config.getBoolean(newpath + ".name.dynamic") : false;

                        String name1 = "";
                        if (!isChildNI) {
                            name1 = (config.getString(newpath + ".name.display-name") != null) ? config.getString(newpath + ".name.display-name") : "";
                        } else {
                            name1 = (config.getList(newpath + ".name.display-name") != null) ? config.getList(newpath + ".name.display-name").get(0) : "";
                        }
                        boolean isSmallChild = config.getBoolean(newpath + ".small", false);
                        final EntityArmorStand child = createArmorStand(childlocation, name1, isSmallChild, childEnableName);
                        Main.rotate(child, config, newpath);
                        linkedStandsSettings.computeIfAbsent(child, k -> new ArrayList()).add(childNameVisibleMode);
                        linkedStands.get(this).add(child);
                        sendSpawnPacket(player, child);
                        if (config.getYml().contains(newpath + ".name.refresh-interval")) {
                            int interval = config.getInt(newpath + ".name.refresh-interval");
                            List<String> names1 = config.getList(newpath + ".name.display-name");
                            (new BukkitRunnable() {
                                int i = 1;
                                public void run() {
                                    if (!owner.isOnline() || removed) {
                                        cancel();
                                        return;
                                    }
                                    if (!isChildNI) {
                                        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(child.getId(), child.getDataWatcher(), true);
                                        child.setCustomName(Main.p(owner, ChatColor.translateAlternateColorCodes('&', config.getString(newpath + ".name.display-name"))));
                                        (((CraftPlayer) owner).getHandle()).playerConnection.sendPacket(packet);
                                    } else {
                                        if (i >= names1.size()) {
                                            i = 0;
                                        }
                                        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(child.getId(), child.getDataWatcher(), true);
                                        child.setCustomName(Main.p(owner, ChatColor.translateAlternateColorCodes('&', names1.get(i))));
                                        (((CraftPlayer) owner).getHandle()).playerConnection.sendPacket(packet);
                                        i ++;

                                    }
                                }
                            }).runTaskTimer(Main.handler.plugin, 0L, interval);
                        }

                        if (config.getYml().contains(newpath + ".icon")) {

                            boolean isChildDI = (config.getYml().contains(newpath + ".icon.dynamic")) ? config.getBoolean(newpath + ".icon.dynamic") : false;
                            linkedStandsSettings.get(child).add(String.valueOf(isChildDI));
                            if (!isChildDI) {
                                ItemStack childicon = Main.handler.createItem(XMaterial.valueOf(config.getString(newpath + ".icon.material")).get(), config.getString(newpath + ".icon.head_value"), config.getInt(newpath + ".icon.data"), "", new ArrayList(), false);
                                ItemSlot slot = ItemSlot.valueOf(config.getString(newpath + ".icon.slot", "HEAD"));

                                TIcon finalIcon = new Icon(childicon, slot);
                                equipIcon(child, finalIcon);
                                linkedStandsIcon.put(child, finalIcon);

                            } else {

                                List<String> iconPaths = new ArrayList<>(config.getYml().getConfigurationSection(newpath + ".icon.dynamic-icons").getKeys(false));
                                List<TIcon> icons = new ArrayList<>();
                                for (String iconPath : iconPaths) {
                                    String thisIconPath = newpath + ".icon.dynamic-icons." + iconPath;
                                    String iconMaterial = config.getString(thisIconPath + ".material");
                                    String iconHeadValue = config.getString(thisIconPath + ".head_value");
                                    int iconData = (config.getYml().contains(thisIconPath + ".data")) ? config.getInt(thisIconPath + ".data") : 0;
                                    boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
                                    ItemStack thisIcon = Main.handler.createItem(XMaterial.valueOf(iconMaterial).get(), iconHeadValue, iconData, "", new ArrayList<>(), isGlowing);
                                    ItemSlot slot = ItemSlot.valueOf(config.getString(thisIconPath + ".slot", "HEAD"));

                                    icons.add(new Icon(thisIcon, slot));
                                }

                                equipIcon(child, icons.get(0));
                                linkedStandsIcon.put(child, icons.get(0));

                                if (config.getYml().contains(newpath + ".icon.refresh-interval")) {
                                    int interval = config.getInt(newpath + ".icon.refresh-interval");
                                    (new BukkitRunnable() {
                                        int i = 1;

                                        public void run() {
                                            if (!Button.this.owner.isOnline() || Button.this.removed) {
                                                cancel();
                                                return;
                                            }
                                            if (i >= icons.size()) {
                                                i = 0;
                                            }
                                            if (!isHiding) {
                                                equipIcon(child, icons.get(i));
                                                linkedStandsIcon.put(child, icons.get(i));
                                            }
                                            i++;

                                        }
                                    }).runTaskTimer(Main.handler.plugin, 0L, interval);

                                }
                            }
                        }
                    }
                }

            }
        }
        boolean moveForward = (config.getYml().contains(path + ".move-forward")) ? config.getBoolean(path + ".move-forward") : true;
        if (moveForward) {
            ButtonSound hoverSound;
            if (config.getYml().contains(path+".hover-sound")) {
                hoverSound = new ButtonSound(XSound.of(config.getString(path + ".hover-sound.sound")).get().get(), (float) config.getDouble(path + ".hover-sound.yaw"), (float) config.getDouble(path + ".hover-sound.pitch"));
            } else {
                hoverSound = null;
            }
            this.task = Bukkit.getScheduler().runTaskTimer(Main.handler.plugin, () -> {
                if (owner == null || armorstand == null || !owner.isOnline()) {
                    cancelTask();
                    return;
                }
                if (linkedStands == null || !linkedStands.containsKey(this)) {
                    cancelTask();
                    return;
                }

                if (owner.getLocation().distance(armorstand.getBukkitEntity().getLocation()) > 5 || isHiding) {
                    if (isMoved) {
                        moveBackward();
                        if (nameVisibleMode.equals("hover")) {
                            sendNameVisibilityPacket(owner, armorstand, false);
                        }
                        for (EntityArmorStand stand : linkedStands.get(this)) {
                            if (linkedStandsSettings.get(stand).get(0).equals("hover")) {
                                sendNameVisibilityPacket(owner, stand, false);
                            }
                        }
                        unmovablePeriod(500);
                        isMoved = false;
                    }
                    return;
                }

                Location playerEye = owner.getEyeLocation();
                Vector playerDirection = playerEye.getDirection().normalize();
                Vector playerPosition = playerEye.toVector();

                Vector armorStandPosition = new Vector(armorstand.locX, armorstand.locY + 1.6, armorstand.locZ);

                Vector toArmorStand = armorStandPosition.subtract(playerPosition).normalize();

                double dotProduct = playerDirection.dot(toArmorStand);

                if (dotProduct > 0.98) {
                    if (!isMoved && moveable) {
                        moveForward();
                        if (hoverSound != null) {
                            Main.handler.playSound(getOwner(), getOwner().getLocation(), hoverSound.getSoundString(), hoverSound.getYaw(), hoverSound.getPitch());
                        }
                        if (nameVisibleMode.equals("hover")) {
                            sendNameVisibilityPacket(owner, armorstand, true);
                        }
                        for (EntityArmorStand stand : linkedStands.get(this)) {
                            if (linkedStandsSettings.get(stand).get(0).equals("hover")) {
                                sendNameVisibilityPacket(owner, stand, true);
                            }
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
                        for (EntityArmorStand stand : linkedStands.get(this)) {
                            if (linkedStandsSettings.get(stand).get(0).equals("hover")) {
                                sendNameVisibilityPacket(owner, stand, false);
                            }
                        }
                        unmovablePeriod(500);
                        isMoved = false;
                    }
                }
            }, 0L, 5L);
        } else {
            this.task = null;
        }
    }

    private EntityArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.p(owner, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setSmall(isSmall);

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
    public void equipIcon(TIcon icon) {
        equipIcon(armorstand, icon);
    }

    public void equipIcon(EntityArmorStand armorStand, TIcon icon) {
        Main.nmsUtil.equipIcon(owner, armorStand, icon.getItemStack(), icon.getItemSlot());
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
            removed = true;
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
        clickable = true;
        Animations.MoveForward(owner, armorstand, (float) 0.5);
        for (EntityArmorStand stand : linkedStands.get(this)) {
            Animations.MoveForward(owner, stand, (float) 0.5);
        }
    }

    public void moveBackward() {
        clickable = false;
        Animations.MoveBackward(owner, armorstand, (float) 0.5);
        for (EntityArmorStand stand : linkedStands.get(this)) {
            Animations.MoveBackward(owner, stand, (float) 0.5);
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
        if (this.task != null) {
            this.task.cancel();
        }
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

    public ButtonSound getSound() {
        return sound;
    }

    public boolean isHiding() {
        return isHiding;
    }

    public void setIcon(TIcon icon) {
        this.icon = icon;
    }

    public TConfigManager getConfig() {
        return config;
    }
    public BukkitTask getTask() {
        return task;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }


    public void setType(ButtonType type) {
        this.type = type;
    }


    public void setActions(List<String> actions) {
        this.actions = actions;
    }


    public void setSound(ButtonSound sound) {
        this.sound = sound;
    }

    public void setHidingState(boolean hiding) {
        isHiding = hiding;
    }

    public TIcon getIcon() {
        return icon;
    }

    public void setConfig(TConfigManager config) {
        this.config = config;
    }

    public String getPathToButton() {
        return pathToButton;
    }

    public void setPathToButton(String pathToButton) {
        this.pathToButton = pathToButton;
    }

    public boolean isDynamicName() {
        return dynamicName;
    }

    public boolean isDynamicIcon() {
        return dynamicIcon;
    }

    public String getNameVisibleMode() {
        return nameVisibleMode;
    }
}
package org.twightlight.hlootchest.supports.v1_19_R3.buttons;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.lootchest.ButtonSpawnEvent;
import org.twightlight.hlootchest.api.interfaces.TButton;
import org.twightlight.hlootchest.utils.ButtonSound;
import org.twightlight.hlootchest.api.interfaces.TConfigManager;
import org.twightlight.hlootchest.supports.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.v1_19_R3.utilities.Animations;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Button implements TButton {
    private final int id;

    private final Player owner;

    private final ArmorStand armorstand;

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

    private boolean dynamicName;

    private boolean dynamicIcon;

    private String nameVisibleMode = "always";

    private boolean removed = false;

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    public static final Map<ArmorStand, List<String>> linkedStandsSettings = new HashMap<>();

    public static final Map<TButton, List<ArmorStand>> linkedStands = new HashMap<>();

    public static final Map<ArmorStand, ItemStack> linkedStandsIcon = new HashMap<>();

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
        dynamicName = (config.getYml().contains(path + ".name.dynamic")) ? config.getBoolean(path + ".name.dynamic") : false;
        String name = "";
        if (!dynamicName) {
            name = (config.getString(path + ".name.display-name") != null) ? config.getString(path + ".name.display-name") : "";
        } else {
            name = (config.getList(path + ".name.display-name") != null) ? config.getList(path + ".name.display-name").get(0) : "";
        }
        final ArmorStand armorStand = createArmorStand(location, name, enableName);
        this.id = (armorStand).getEntityId();
        this.armorstand = armorStand;
        Main.rotate(this.armorstand, config, path);
        buttonIdMap.put(Integer.valueOf(this.id), this);
        playerButtonMap.computeIfAbsent(player, k -> new ArrayList()).add(this);
        linkedStands.put(this, new ArrayList<>());
        this.type = type;
        sendSpawnPacket(player, armorStand);
        if (config.getYml().contains(path + ".name.refresh-interval")) {
            int interval = config.getInt(path + ".name.refresh-interval");
            List<String> names = config.getList(path + ".name.display-name");
            (new BukkitRunnable() {
                int i = 1;
                public void run() {
                    if (!Button.this.owner.isOnline() || Button.this.removed) {
                        cancel();
                        return;
                    }
                    if (!dynamicName) {
                        armorstand.setCustomName(Main.p(Button.this.owner, config.getString(path + ".name.display-name")));

                    } else {
                        if (i >= names.size()) {
                            i = 0;
                        }
                        armorstand.setCustomName(Main.p(Button.this.owner, names.get(i)));
                        i ++;

                    }
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
                    Animations.spin(Button.this.owner, armorstand, location.clone().getYaw() - angle * this.i * multiply);
                }
            }).runTaskTimer(Main.handler.plugin, 2L, 1L);
        }
        boolean isHoldingIcon = config.getYml().contains(path + ".holding-icon") ? config.getBoolean(pathToButton + ".holding-icon") : true;
        if (isHoldingIcon) {
            equipIcon(armorStand, icon);
            this.icon = icon;
        }

        dynamicIcon = (config.getYml().contains(path + ".icon.dynamic")) ? config.getBoolean(path + ".icon.dynamic") : false;

        if (isHoldingIcon && dynamicIcon && config.getYml().contains(path + ".icon.refresh-interval")) {
            int interval = config.getInt(path + ".icon.refresh-interval");
            List<String> iconPaths = new ArrayList<>(config.getYml().getConfigurationSection(path + ".icon.dynamic-icons").getKeys(false));
            List<ItemStack> icons = new ArrayList<>();
            for (String iconPath : iconPaths) {
                String thisIconPath = path + ".icon.dynamic-icons." + iconPath;
                String iconMaterial = config.getString(thisIconPath + ".material");
                String iconHeadValue = config.getString(thisIconPath + ".head_value");
                int iconData = (config.getYml().contains(thisIconPath + ".data")) ? config.getInt(thisIconPath + ".data") : 0;
                boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
                ItemStack thisIcon = Main.handler.createItem(XMaterial.valueOf(iconMaterial).parseMaterial(), iconHeadValue, iconData, "", new ArrayList<>(), isGlowing);
                icons.add(thisIcon);
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
                final String newpath = path + ".children." + childname;
                if (Main.api.getPlayerUtil().checkConditions(player, config, newpath + ".spawn-requirements")) {
                    Location childlocation = null;
                    if (config.getString(newpath + ".location") != null) {
                        childlocation = Main.handler.stringToLocation(config.getString(newpath + ".location"));
                    } else if (config.getString(newpath + ".location-offset") != null) {
                        String[] offsetXYZ = config.getString(newpath + ".location-offset").split(",");
                        double yaw = this.armorstand.getLocation().getYaw();
                        Vector vector = this.armorstand.getLocation().getDirection().normalize();
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
                        boolean childEnableName = config.getYml().contains(newpath + ".name") ? config.getBoolean(newpath + ".name.enable") : false;
                        String childNameVisibleMode = "always";
                        if (config.getYml().contains(newpath + ".name.visible-mode") && childEnableName) {
                            String mode = config.getString(newpath + ".name.visible-mode");
                            if (mode.equals("hover"))
                                childEnableName = false;
                            childNameVisibleMode = mode;
                        }
                        boolean isChildNI = (config.getYml().contains(newpath + ".name.dynamic")) ? config.getBoolean(newpath + ".name.dynamic") : false;

                        String name1 = "";
                        if (!isChildNI) {
                            name1 = (config.getString(newpath + ".name.display-name") != null) ? config.getString(newpath + ".name.display-name") : "";
                        } else {
                            name1 = (config.getList(newpath + ".name.display-name") != null) ? config.getList(newpath + ".name.display-name").get(0) : "";
                        }
                        final ArmorStand child = createArmorStand(childlocation, name1, childEnableName);
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
                                        child.setCustomName(Main.p(owner, config.getString(newpath + ".name.display-name")));

                                    } else {
                                        if (i >= names1.size()) {
                                            i = 0;
                                        }
                                        child.setCustomName(Main.p(owner, names1.get(i)));
                                        i ++;

                                    }
                                }
                            }).runTaskTimer(Main.handler.plugin, 0L, interval);
                        }
                        if (config.getYml().contains(newpath + ".icon")) {

                            boolean isChildDI = (config.getYml().contains(newpath + ".icon.dynamic")) ? config.getBoolean(newpath + ".icon.dynamic") : false;

                            linkedStandsSettings.get(child).add(String.valueOf(isChildDI));

                            if (!isChildDI) {
                                ItemStack childicon = Main.handler.createItem(XMaterial.valueOf(config.getString(newpath + ".icon.material")).parseMaterial(), config.getString(newpath + ".icon.head_value"), config.getInt(newpath + ".icon.data"), "", new ArrayList(), false);
                                equipIcon(child, childicon);
                                linkedStandsIcon.put(child, childicon);

                            } else {
                                List<String> iconPaths = new ArrayList<>(config.getYml().getConfigurationSection(newpath + ".icon.dynamic-icons").getKeys(false));
                                List<ItemStack> icons = new ArrayList<>();
                                for (String iconPath : iconPaths) {
                                    String thisIconPath = newpath + ".icon.dynamic-icons." + iconPath;
                                    String iconMaterial = config.getString(thisIconPath + ".material");
                                    String iconHeadValue = config.getString(thisIconPath + ".head_value");
                                    int iconData = (config.getYml().contains(thisIconPath + ".data")) ? config.getInt(thisIconPath + ".data") : 0;
                                    boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
                                    ItemStack thisIcon = Main.handler.createItem(XMaterial.valueOf(iconMaterial).parseMaterial(), iconHeadValue, iconData, "", new ArrayList<>(), isGlowing);
                                    icons.add(thisIcon);
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
                if (!linkedStands.containsKey(this)) {
                    cancelTask();
                    return;
                }
                if (this.owner.getLocation().distance(this.armorstand.getLocation()) > 5.0D || this.isHiding) {
                    if (this.isMoved) {
                        if (this.nameVisibleMode.equals("hover"))
                            sendNameVisibilityPacket(this.armorstand ,false);
                        for (ArmorStand stand : linkedStands.get(this)) {
                            if ((linkedStandsSettings.get(stand)).get(0).equals("hover"))
                                sendNameVisibilityPacket(stand, false);
                        }
                        moveBackward();
                        unmovablePeriod(500);
                        this.isMoved = false;
                    }
                    return;
                }
                Location playerEye = this.owner.getEyeLocation();
                Vector playerDirection = playerEye.getDirection().normalize();
                Vector playerPosition = playerEye.toVector();
                Vector armorStandPosition = new Vector(armorstand.getLocation().getX(), armorstand.getLocation().getY() + 1.6D, armorstand.getLocation().getZ());
                Vector toArmorStand = armorStandPosition.subtract(playerPosition).normalize();
                double dotProduct = playerDirection.dot(toArmorStand);
                if (dotProduct > 0.98D) {
                    if (!this.isMoved && this.moveable) {
                        if (hoverSound != null)
                            Main.handler.playSound(getOwner(), getOwner().getLocation(), hoverSound.getSoundString(), hoverSound.getYaw(), hoverSound.getPitch());
                        if (this.nameVisibleMode.equals("hover"))
                            sendNameVisibilityPacket(this.armorstand, true);
                        for (ArmorStand stand : linkedStands.get(this)) {
                            if ((linkedStandsSettings.get(stand)).get(0).equals("hover"))
                                sendNameVisibilityPacket(stand, true);
                        }
                        moveForward();
                        unmovablePeriod(500);
                        this.isMoved = true;
                    }
                } else if (this.isMoved && this.moveable) {

                    if (this.nameVisibleMode.equals("hover"))
                        sendNameVisibilityPacket(armorstand, false);
                    for (ArmorStand stand : linkedStands.get(this)) {
                        if ((linkedStandsSettings.get(stand)).get(0).equals("hover"))
                            sendNameVisibilityPacket(stand, false);
                    }
                    moveBackward();
                    unmovablePeriod(500);
                    this.isMoved = false;
                }
            }, 0L, 5L);
        } else {
            this.task = null;
        }
    }

    private ArmorStand createArmorStand(Location location, String name, boolean isNameEnable) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.p(this.owner, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setVisibleByDefault(false);
        return armorStand;
    }

    private void sendSpawnPacket(Player player, ArmorStand armorStand) {
        player.showEntity(Main.handler.plugin, armorStand);
    }

    private void sendDespawnPacket(Player player, ArmorStand armorStand) {
        player.hideEntity(Main.handler.plugin, armorStand);
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
            EquipmentSlot slot = EquipmentSlot.HEAD;
            if (this.type == ButtonType.REWARD) {
                slot = EquipmentSlot.HAND;
            }

            armorstand.getEquipment().setItem(slot, bukkiticon);
        }
    }

    private void equipIcon(ArmorStand armorStand, ItemStack bukkiticon) {
        if (bukkiticon != null) {
            EquipmentSlot slot = EquipmentSlot.HEAD;
            if (this.type == ButtonType.REWARD) {
                slot = EquipmentSlot.HAND;
            }

            armorStand.getEquipment().setItem(slot, bukkiticon);
        }
    }

    public void hide(boolean isHiding) {
        this.isHiding = isHiding;
        if (isHiding) {
            sendDespawnPacket(this.owner, this.armorstand);
            for (ArmorStand stand : linkedStands.get(this))
                sendDespawnPacket(this.owner, stand);
        } else {
            sendSpawnPacket(this.owner, this.armorstand);
            equipIcon(this.armorstand, this.icon);
            for (ArmorStand stand : linkedStands.get(this)) {
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
            armorstand.remove();
            for (ArmorStand stand : linkedStands.get(this)) {
                stand.remove();
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
        Animations.moveForward(this.owner, this.armorstand, 0.5F);
        for (ArmorStand stand : linkedStands.get(this))
            Animations.moveForward(this.owner, stand, 0.5F);
    }

    public void moveBackward() {
        this.clickable = false;
        Animations.moveBackward(this.owner, this.armorstand, 0.5F);
        for (ArmorStand stand : linkedStands.get(this))
            Animations.moveBackward(this.owner, stand, 0.5F);
    }

    public void sendNameVisibilityPacket(ArmorStand armorStand, boolean visible) {
        armorStand.setCustomNameVisible(visible);
    }

    public int getCustomId() {
        return this.id;
    }

    public Player getOwner() {
        return this.owner;
    }

    public ArmorStand getEntity() {
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

    public void setIcon(ItemStack icon) {
        this.icon = icon;
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

    public ItemStack getIcon() {
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

    public TConfigManager getConfig() {
        return config;
    }

}
package org.twightlight.hlootchest.supports.protocol.v1_19_R3.buttons;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;
import org.twightlight.libs.exp4j.Expression;
import org.twightlight.libs.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.utilities.Animations;
import org.twightlight.hlootchest.utils.Utility;

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

    private TIcon icon = null;

    private TConfigManager config;

    private String pathToButton;

    private boolean dynamicName;

    private boolean dynamicIcon;

    private boolean isPreview;

    private String nameVisibleMode = "always";

    private boolean removed = false;

    private boolean better_check_algorithm;

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    public static final Map<ArmorStand, List<String>> linkedStandsSettings = new HashMap<>();

    public static final Map<TButton, List<ArmorStand>> linkedStands = new HashMap<>();

    public static final Map<ArmorStand, TIcon> linkedStandsIcon = new HashMap<>();

    public Button(final Location location, ButtonType type, Player player, final String path, final TConfigManager config, boolean isPreview) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;
        this.isPreview = isPreview;

        this.better_check_algorithm = Main.api.getConfigUtil().getMainConfig().
                getBoolean("performance.modern-check-algorithm", false);

        ButtonSpawnEvent event = new ButtonSpawnEvent(this.owner, this);
        Bukkit.getPluginManager().callEvent((Event)event);
        this.actions = (config.getList(path + ".actions") != null) ? config.getList(path + ".actions") : new ArrayList<>();
        if (config.getYml().contains(path + ".click-sound"))

            this.sound = new ButtonSound(XSound.of(config.getString(path + ".click-sound.sound")).get().get(), (float)config.getDouble(path + ".click-sound.yaw"), (float)config.getDouble(path + ".click-sound.pitch"));

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
        boolean isSmall = config.getBoolean(path + ".small", false);
        final ArmorStand armorStand = createArmorStand(location, name, isSmall, enableName);
        this.id = (armorStand).getEntityId();
        this.armorstand = armorStand;
        Main.nmsUtil.rotate(this.armorstand, config, path);
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
                        armorstand.setCustomName(Main.api.getLanguageUtil().p(Button.this.owner, config.getString(path + ".name.display-name")));

                    } else {
                        if (i >= names.size()) {
                            i = 0;
                        }
                        armorstand.setCustomName(Main.api.getLanguageUtil().p(Button.this.owner, names.get(i)));
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
        boolean isHoldingIcon = (config.getYml().contains(path + ".holding-icon")) ? config.getBoolean(pathToButton + ".holding-icon") : true;

        dynamicIcon = (config.getYml().contains(path + ".icon.dynamic")) ? config.getBoolean(path + ".icon.dynamic") : false;
        if (isHoldingIcon && !dynamicIcon) {
            String thisIconPath = pathToButton + ".icon";
            String iconMaterial = config.getString(thisIconPath + ".material");
            String iconHeadValue = config.getString(thisIconPath + ".head_value");
            int iconData = config.getYml().contains(thisIconPath + ".data") ?
                    config.getInt(thisIconPath + ".data") : 0;
            boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
            ItemSlot slot = ItemSlot.valueOf(config.getString(thisIconPath + ".slot", "HEAD"));

            ItemStack icon = Main.handler.createItem(
                    XMaterial.valueOf(iconMaterial).get(),
                    iconHeadValue,
                    iconData,
                    "",
                    new ArrayList<>(),
                    isGlowing
            );

            TIcon ticon = new Icon(icon, slot);
            equipIcon(armorstand, ticon);
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
                final String newpath = path + ".children." + childname;
                if (Main.api.getPlayerUtil().checkConditions(player, config, newpath + ".spawn-requirements")) {
                    Location childlocation = null;
                    if (config.getString(newpath + ".location") != null) {
                        childlocation = Utility.stringToLocation(config.getString(newpath + ".location"));
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
                        boolean isSmallChild = config.getBoolean(newpath + ".small", false);
                        final ArmorStand child = createArmorStand(childlocation, name1, isSmallChild, childEnableName);
                        Main.nmsUtil.rotate(child, config, newpath);
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
                                        child.setCustomName(Main.api.getLanguageUtil().p(owner, config.getString(newpath + ".name.display-name")));

                                    } else {
                                        if (i >= names1.size()) {
                                            i = 0;
                                        }
                                        child.setCustomName(Main.api.getLanguageUtil().p(owner, names1.get(i)));
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
        boolean moveForward = config.getYml().contains(path + ".move-forward") ? config.getBoolean(path + ".move-forward") : true;
        if (moveForward) {
            ButtonSound hoverSound;
            if (config.getYml().contains(path + ".hover-sound")) {
                hoverSound = new ButtonSound(XSound.of(config.getString(path + ".hover-sound.sound")).get().get(), (float)config.getDouble(path + ".hover-sound.yaw"), (float)config.getDouble(path + ".hover-sound.pitch"));
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
                if (isPlayerLookingAtButton(better_check_algorithm)) {
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

    private boolean isPlayerLookingAtButton(boolean better_check_algorithm) {
        if (better_check_algorithm) {
            double maxDistance = 5.0;

            Location eyeLoc = owner.getEyeLocation();
            Vector rayOrigin = eyeLoc.toVector();
            Vector rayDirection = eyeLoc.getDirection().normalize();

            double x = armorstand.getLocation().getX();
            double y = armorstand.getLocation().getY();
            double z = armorstand.getLocation().getZ();

            Vector min = new Vector(x - 0.3, y + 1.6 - 0.1875, z - 0.3);
            Vector max = new Vector(x + 0.3, y + 2.05, z + 0.3);

            return rayIntersectsAABB(rayOrigin, rayDirection, min, max, maxDistance);
        } else {
            Location playerEye = owner.getEyeLocation();
            Vector playerDirection = playerEye.getDirection().normalize();
            Vector playerPosition = playerEye.toVector();
            Vector armorStandPosition = new Vector(armorstand.getLocation().getX(), armorstand.getLocation().getY() + 1.6, armorstand.getLocation().getZ());
            Vector toArmorStand = armorStandPosition.subtract(playerPosition).normalize();

            return playerDirection.dot(toArmorStand) > 0.98;
        }

    }

    private boolean rayIntersectsAABB(Vector rayOrigin, Vector rayDir, Vector min, Vector max, double maxDistance) {
        double tMin = 0.0;
        double tMax = maxDistance;

        for (int i = 0; i < 3; i++) {
            double origin = 0, direction = 0, minVal = 0, maxVal = 0;

            switch (i) {
                case 0:
                    origin = rayOrigin.getX();
                    direction = rayDir.getX();
                    minVal = min.getX();
                    maxVal = max.getX();
                    break;
                case 1:
                    origin = rayOrigin.getY();
                    direction = rayDir.getY();
                    minVal = min.getY();
                    maxVal = max.getY();
                    break;
                case 2:
                    origin = rayOrigin.getZ();
                    direction = rayDir.getZ();
                    minVal = min.getZ();
                    maxVal = max.getZ();
                    break;
            }

            double invD = 1.0 / direction;
            double t0 = (minVal - origin) * invD;
            double t1 = (maxVal - origin) * invD;

            if (invD < 0.0) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }

            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);

            if (tMax < tMin) return false;
        }
        return true;
    }

    private ArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.api.getLanguageUtil().p(this.owner, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setSmall(isSmall);
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

    public void equipIcon(TIcon icon) {
        equipIcon(armorstand, icon);
    }

    public void equipIcon(ArmorStand armorStand, TIcon icon) {
        Main.nmsUtil.equipIcon(owner, armorStand, icon.getItemStack(), icon.getItemSlot());
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

    public void setIcon(TIcon icon) {
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

    public TConfigManager getConfig() {
        return config;
    }

    public boolean isPreview() {
        return isPreview;
    }
}
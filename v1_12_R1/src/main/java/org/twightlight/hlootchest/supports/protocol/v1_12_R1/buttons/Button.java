package org.twightlight.hlootchest.supports.protocol.v1_12_R1.buttons;

import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;
import net.minecraft.server.v1_12_R1.*;
import org.twightlight.libs.exp4j.Expression;
import org.twightlight.libs.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
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
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.Main;
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.utilities.Animations;
import org.twightlight.hlootchest.utils.Utility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Button implements TButton {
    private int id;
    private final Player owner;
    private EntityArmorStand armorstand;
    private boolean isMoved = false;
    private BukkitTask task;
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

    public static final Map<EntityArmorStand, List<String>> linkedStandsSettings = new HashMap<>();
    public static final Map<TButton, List<EntityArmorStand>> linkedStands = new HashMap<>();
    public static final Map<EntityArmorStand, TIcon> linkedStandsIcon = new HashMap<>();

    public Button(Location location, ButtonType type, Player player, String path, TConfigManager config, boolean isPreview) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;
        this.isPreview = isPreview;

        this.better_check_algorithm = Main.api.getConfigUtil().getMainConfig().
                getBoolean("performance.modern-check-algorithm", false);

        callSpawnEvent();
        loadButtonSettings();
        initializeArmorStand(location);
        handleButtonType(type);

        setupNameRefresh();
        setupRotationOnSpawn(location);
        setupIconHandling();

        setupChildButtons(location);

        setupMovementBehavior();
        unmovablePeriod(250);
    }

    private void callSpawnEvent() {
        ButtonSpawnEvent event = new ButtonSpawnEvent(owner, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void loadButtonSettings() {
        this.actions = (config.getList(pathToButton+".actions") != null) ?
                config.getList(pathToButton+".actions") : new ArrayList<>();

        if (config.getYml().contains(pathToButton+".click-sound")) {
            this.sound = new ButtonSound(
                    XSound.of(config.getString(pathToButton + ".click-sound.sound")).get().get(),
                    (float) config.getDouble(pathToButton + ".click-sound.yaw"),
                    (float) config.getDouble(pathToButton + ".click-sound.pitch")
            );
        }
    }

    private void initializeArmorStand(Location location) {
        boolean enableName = config.getYml().contains(pathToButton+".name") ?
                config.getBoolean(pathToButton+".name.enable") : false;

        if (config.getYml().contains(pathToButton+".name.visible-mode") && enableName) {
            String mode = config.getString(pathToButton+".name.visible-mode");
            if (mode.equals("hover")) {
                enableName = false;
            }
            nameVisibleMode = mode;
        }

        dynamicName = config.getYml().contains(pathToButton + ".name.dynamic") ?
                config.getBoolean(pathToButton + ".name.dynamic") : false;

        String name = getInitialName();
        boolean isSmall = config.getBoolean(pathToButton + ".small", false);

        final EntityArmorStand armorStand = createArmorStand(location, name, isSmall, enableName);
        this.id = armorStand.getId();
        this.armorstand = armorStand;

        Main.nmsUtil.rotate(armorstand, config, pathToButton);
        buttonIdMap.put(this.id, this);
        playerButtonMap.computeIfAbsent(owner, k -> new ArrayList<>()).add(this);
        linkedStands.put(this, new ArrayList<>());

        sendSpawnPacket(owner, armorStand);
    }

    private String getInitialName() {
        if (!dynamicName) {
            return config.getString(pathToButton + ".name.display-name") != null ?
                    config.getString(pathToButton + ".name.display-name") : "";
        } else {
            return config.getList(pathToButton + ".name.display-name") != null ?
                    config.getList(pathToButton + ".name.display-name").get(0) : "";
        }
    }

    private void handleButtonType(ButtonType type) {
        this.type = type;
    }

    private void setupNameRefresh() {
        if (!config.getYml().contains(pathToButton + ".name.refresh-interval")) return;

        int interval = config.getInt(pathToButton + ".name.refresh-interval");
        List<String> names = config.getList(pathToButton + ".name.display-name");

        new BukkitRunnable() {
            int i = 1;
            public void run() {
                if (!owner.isOnline() || removed) {
                    cancel();
                    return;
                }

                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
                        armorstand.getId(), armorstand.getDataWatcher(), true);

                if (!dynamicName) {
                    armorstand.setCustomName(Main.api.getLanguageUtil().p(owner, config.getString(pathToButton + ".name.display-name")));
                } else {
                    if (i >= names.size()) i = 0;
                    armorstand.setCustomName(Main.api.getLanguageUtil().p(owner, names.get(i)));
                    i++;
                }

                (((CraftPlayer)owner).getHandle()).playerConnection.sendPacket(packet);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, interval);
    }

    private void setupRotationOnSpawn(Location location) {
        if (!config.getYml().contains(pathToButton+".rotate-on-spawn")) return;

        boolean rotateOnSpawn = config.getBoolean(pathToButton+".rotate-on-spawn.enable");
        if (!rotateOnSpawn) return;

        float angle = (float) (location.clone().getYaw() - config.getDouble(pathToButton+".rotate-on-spawn.final-yaw"));
        boolean reverse = config.getBoolean(pathToButton+".rotate-on-spawn.reverse");

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (!owner.isOnline() || i >= 1) {
                    this.cancel();
                    return;
                }

                i++;
                int multiply = reverse ? 1 : -1;
                Animations.Spinning(owner, armorstand, location.clone().getYaw() - (angle * i * multiply));
            }
        }.runTaskTimer(Main.handler.plugin, 2L, 1L);
    }

    private void setupIconHandling() {
        boolean isHoldingIcon = config.getYml().contains(pathToButton + ".holding-icon") ?
                config.getBoolean(pathToButton + ".holding-icon") : true;

        dynamicIcon = config.getYml().contains(pathToButton + ".icon.dynamic") ?
                config.getBoolean(pathToButton + ".icon.dynamic") : false;

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

        if (isHoldingIcon && dynamicIcon && config.getYml().contains(pathToButton + ".icon.refresh-interval")) {
            setupDynamicIconRefresh();
        }
    }

    private void setupDynamicIconRefresh() {
        int interval = config.getInt(pathToButton + ".icon.refresh-interval");
        List<String> iconPaths = new ArrayList<>(
                config.getYml().getConfigurationSection(pathToButton + ".icon.dynamic-icons").getKeys(false));

        List<TIcon> icons = new ArrayList<>();
        for (String iconPath : iconPaths) {
            String thisIconPath = pathToButton + ".icon.dynamic-icons." + iconPath;
            String iconMaterial = config.getString(thisIconPath + ".material");
            String iconHeadValue = config.getString(thisIconPath + ".head_value");
            int iconData = config.getYml().contains(thisIconPath + ".data") ?
                    config.getInt(thisIconPath + ".data") : 0;
            boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
            ItemSlot slot = ItemSlot.valueOf(config.getString(thisIconPath + ".slot", "HEAD"));

            ItemStack thisIcon = Main.handler.createItem(
                    XMaterial.valueOf(iconMaterial).get(),
                    iconHeadValue,
                    iconData,
                    "",
                    new ArrayList<>(),
                    isGlowing
            );
            icons.add(new Icon(thisIcon, slot));
        }

        new BukkitRunnable() {
            int i = 1;
            public void run() {
                if (!Button.this.owner.isOnline() || Button.this.removed) {
                    cancel();
                    return;
                }
                if (i >= icons.size()) i = 0;
                if (!isHiding) {
                    equipIcon(armorstand, icons.get(i));
                    setIcon(icons.get(i));
                }
                i++;
            }
        }.runTaskTimer(Main.handler.plugin, 0L, interval);
    }

    private void setupChildButtons(Location parentLocation) {
        if (config.getYml().getConfigurationSection(pathToButton + ".children") == null) return;

        Set<String> linkeds = config.getYml().getConfigurationSection(pathToButton + ".children").getKeys(false);

        for (String childname : linkeds) {
            String newpath = pathToButton + ".children" + "." + childname;

            if (!Main.api.getPlayerUtil().checkConditions(owner, config, newpath + ".spawn-requirements")) {
                continue;
            }

            Location childlocation = calculateChildLocation(parentLocation, newpath);
            if (childlocation == null) continue;

            setupChildArmorStand(newpath, childlocation);
        }
    }

    private Location calculateChildLocation(Location parentLocation, String newpath) {
        if (config.getString(newpath + ".location") != null) {
            return Utility.stringToLocation(config.getString(newpath + ".location"));
        }

        if (config.getString(newpath + ".location-offset") != null) {
            return calculateOffsetLocation(parentLocation, newpath);
        }

        return null;
    }

    private Location calculateOffsetLocation(Location parentLocation, String newpath) {
        String[] offsetXYZ = config.getString(newpath + ".location-offset").split(",");
        double yaw = this.armorstand.yaw;
        Vector vector = this.armorstand.getBukkitEntity().getLocation().getDirection().normalize();

        if (offsetXYZ.length == 3) {
            Expression exp = buildExpression(offsetXYZ[0], yaw, vector);
            Expression exp1 = buildExpression(offsetXYZ[1], yaw, vector);
            Expression exp2 = buildExpression(offsetXYZ[2], yaw, vector);

            return parentLocation.clone().add(exp.evaluate(), exp1.evaluate(), exp2.evaluate());
        }

        if (offsetXYZ.length == 5) {
            Expression exp = buildExpression(offsetXYZ[0], yaw, vector);
            Expression exp1 = buildExpression(offsetXYZ[1], yaw, vector);
            Expression exp2 = buildExpression(offsetXYZ[2], yaw, vector);
            Expression exp3 = new ExpressionBuilder(offsetXYZ[3]).build();
            Expression exp4 = new ExpressionBuilder(offsetXYZ[4]).build();

            Location locClone = parentLocation.clone();
            locClone.setYaw(parentLocation.getYaw() + (float) exp3.evaluate());
            locClone.setPitch(parentLocation.getPitch() + (float) exp4.evaluate());
            return locClone.add(exp.evaluate(), exp1.evaluate(), exp2.evaluate());
        }

        return null;
    }

    private Expression buildExpression(String expression, double yaw, Vector vector) {
        return new ExpressionBuilder(expression)
                .variables(new String[]{"yaw", "VectorX", "VectorZ"})
                .build()
                .setVariable("yaw", Math.toRadians(yaw))
                .setVariable("VectorX", vector.getX())
                .setVariable("VectorZ", vector.getZ());
    }

    private void setupChildArmorStand(String newpath, Location childlocation) {
        boolean childEnableName = config.getYml().contains(newpath + ".name") ?
                config.getBoolean(newpath + ".name.enable") : false;

        String childNameVisibleMode = setupChildNameVisibility(newpath, childEnableName);
        boolean isChildNI = config.getYml().contains(newpath + ".name.dynamic") ?
                config.getBoolean(newpath + ".name.dynamic") : false;

        String name1 = getChildName(newpath, isChildNI);
        boolean isSmallChild = config.getBoolean(newpath + ".small", false);

        final EntityArmorStand child = createArmorStand(childlocation, name1, isSmallChild, childEnableName);
        Main.nmsUtil.rotate(child, config, newpath);

        linkedStandsSettings.computeIfAbsent(child, k -> new ArrayList()).add(childNameVisibleMode);
        linkedStands.get(this).add(child);

        sendSpawnPacket(owner, child);

        if (config.getYml().contains(newpath + ".name.refresh-interval")) {
            setupChildNameRefresh(newpath, isChildNI, child);
        }

        if (config.getYml().contains(newpath + ".icon")) {
            setupChildIcon(newpath, child);
        }
    }

    private String setupChildNameVisibility(String newpath, boolean childEnableName) {
        String childNameVisibleMode = "always";
        if (config.getYml().contains(newpath + ".name.visible-mode") && childEnableName) {
            String mode = config.getString(newpath + ".name.visible-mode");
            if (mode.equals("hover")) {
                childEnableName = false;
            }
            childNameVisibleMode = mode;
        }
        return childNameVisibleMode;
    }

    private String getChildName(String newpath, boolean isChildNI) {
        if (!isChildNI) {
            return config.getString(newpath + ".name.display-name") != null ?
                    config.getString(newpath + ".name.display-name") : "";
        } else {
            return config.getList(newpath + ".name.display-name") != null ?
                    config.getList(newpath + ".name.display-name").get(0) : "";
        }
    }

    private void setupChildNameRefresh(String newpath, boolean isChildNI, EntityArmorStand child) {
        int interval = config.getInt(newpath + ".name.refresh-interval");
        List<String> names1 = config.getList(newpath + ".name.display-name");

        new BukkitRunnable() {
            int i = 1;
            public void run() {
                if (!owner.isOnline() || removed) {
                    cancel();
                    return;
                }

                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
                        child.getId(), child.getDataWatcher(), true);

                if (!isChildNI) {
                    child.setCustomName(Main.api.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&',
                            config.getString(newpath + ".name.display-name"))));
                } else {
                    if (i >= names1.size()) i = 0;
                    child.setCustomName(Main.api.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&', names1.get(i))));
                    i++;
                }

                (((CraftPlayer) owner).getHandle()).playerConnection.sendPacket(packet);
            }
        }.runTaskTimer(Main.handler.plugin, 0L, interval);
    }

    private void setupChildIcon(String newpath, EntityArmorStand child) {
        boolean isChildDI = config.getYml().contains(newpath + ".icon.dynamic") ?
                config.getBoolean(newpath + ".icon.dynamic") : false;

        linkedStandsSettings.get(child).add(String.valueOf(isChildDI));

        if (!isChildDI) {
            setupStaticChildIcon(newpath, child);
        } else {
            setupDynamicChildIcon(newpath, child);
        }
    }

    private void setupStaticChildIcon(String newpath, EntityArmorStand child) {
        ItemStack childicon = Main.handler.createItem(
                XMaterial.valueOf(config.getString(newpath + ".icon.material")).get(),
                config.getString(newpath + ".icon.head_value"),
                config.getInt(newpath + ".icon.data"),
                "",
                new ArrayList(),
                false
        );

        ItemSlot slot = ItemSlot.valueOf(config.getString(newpath + ".icon.slot", "HEAD"));
        TIcon finalIcon = new Icon(childicon, slot);

        equipIcon(child, finalIcon);
        linkedStandsIcon.put(child, finalIcon);
    }

    private void setupDynamicChildIcon(String newpath, EntityArmorStand child) {
        List<String> iconPaths = new ArrayList<>(
                config.getYml().getConfigurationSection(newpath + ".icon.dynamic-icons").getKeys(false));

        List<TIcon> icons = new ArrayList<>();
        for (String iconPath : iconPaths) {
            String thisIconPath = newpath + ".icon.dynamic-icons." + iconPath;
            String iconMaterial = config.getString(thisIconPath + ".material");
            String iconHeadValue = config.getString(thisIconPath + ".head_value");
            int iconData = config.getYml().contains(thisIconPath + ".data") ?
                    config.getInt(thisIconPath + ".data") : 0;
            boolean isGlowing = config.getBoolean(thisIconPath + ".glowing", false);
            ItemSlot slot = ItemSlot.valueOf(config.getString(thisIconPath + ".slot", "HEAD"));

            ItemStack thisIcon = Main.handler.createItem(
                    XMaterial.valueOf(iconMaterial).get(),
                    iconHeadValue,
                    iconData,
                    "",
                    new ArrayList<>(),
                    isGlowing
            );
            icons.add(new Icon(thisIcon, slot));
        }

        equipIcon(child, icons.get(0));
        linkedStandsIcon.put(child, icons.get(0));

        if (config.getYml().contains(newpath + ".icon.refresh-interval")) {
            int interval = config.getInt(newpath + ".icon.refresh-interval");

            new BukkitRunnable() {
                int i = 1;
                public void run() {
                    if (!Button.this.owner.isOnline() || Button.this.removed) {
                        cancel();
                        return;
                    }
                    if (i >= icons.size()) i = 0;
                    if (!isHiding) {
                        equipIcon(child, icons.get(i));
                        linkedStandsIcon.put(child, icons.get(i));
                    }
                    i++;
                }
            }.runTaskTimer(Main.handler.plugin, 0L, interval);
        }
    }

    private void setupMovementBehavior() {
        boolean moveForward = config.getYml().contains(pathToButton + ".move-forward") ?
                config.getBoolean(pathToButton + ".move-forward") : true;

        if (!moveForward) {
            this.task = null;
            return;
        }

        ButtonSound hoverSound = config.getYml().contains(pathToButton+".hover-sound") ?
                new ButtonSound(
                        XSound.of(config.getString(pathToButton + ".hover-sound.sound")).get().get(),
                        (float) config.getDouble(pathToButton + ".hover-sound.yaw"),
                        (float) config.getDouble(pathToButton + ".hover-sound.pitch")
                ) : null;

        this.task = Bukkit.getScheduler().runTaskTimer(Main.handler.plugin, () -> {
            checkButtonMovement(hoverSound);
        }, 0L, 5L);
    }

    private void checkButtonMovement(ButtonSound hoverSound) {
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
                resetButtonPosition(hoverSound);
            }
            return;
        }

        if (isPlayerLookingAtButton(better_check_algorithm)) {
            if (!isMoved && moveable) {
                moveButtonForward(hoverSound);
            }
        } else {
            if (isMoved && moveable) {
                resetButtonPosition(hoverSound);
            }
        }
    }

    private boolean isPlayerLookingAtButton(boolean better_check_algorithm) {
        if (better_check_algorithm) {
            double maxDistance = 5.0;

            Location eyeLoc = owner.getEyeLocation();
            Vector rayOrigin = eyeLoc.toVector();
            Vector rayDirection = eyeLoc.getDirection().normalize();

            double x = armorstand.locX;
            double y = armorstand.locY;
            double z = armorstand.locZ;

            Vector min = new Vector(x - 0.3, y + 1.6 - 0.1875, z - 0.3);
            Vector max = new Vector(x + 0.3, y + 2.05, z + 0.3);

            return rayIntersectsAABB(rayOrigin, rayDirection, min, max, maxDistance);
        } else {
            Location playerEye = owner.getEyeLocation();
            Vector playerDirection = playerEye.getDirection().normalize();
            Vector playerPosition = playerEye.toVector();
            Vector armorStandPosition = new Vector(armorstand.locX, armorstand.locY + 1.6, armorstand.locZ);
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

    private void moveButtonForward(ButtonSound hoverSound) {
        moveForward();
        if (hoverSound != null) {
            Main.handler.playSound(owner, owner.getLocation(),
                    hoverSound.getSoundString(), hoverSound.getYaw(), hoverSound.getPitch());
        }
        updateNameVisibility(true);
        unmovablePeriod(500);
        isMoved = true;
    }

    private void resetButtonPosition(ButtonSound hoverSound) {
        moveBackward();
        updateNameVisibility(false);
        unmovablePeriod(500);
        isMoved = false;
    }

    private void updateNameVisibility(boolean visible) {
        if (nameVisibleMode.equals("hover")) {
            sendNameVisibilityPacket(owner, armorstand, visible);
        }

        for (EntityArmorStand stand : linkedStands.get(this)) {
            if (linkedStandsSettings.get(stand).get(0).equals("hover")) {
                sendNameVisibilityPacket(owner, stand, visible);
            }
        }
    }

    private EntityArmorStand createArmorStand(Location location, String name, boolean isSmall, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.api.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
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
        armorStand.setCustomNameVisible(visible);

        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
                armorStand.getId(), armorStand.getDataWatcher(), true
        );

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

    public boolean isPreview() {
        return isPreview;
    }
}
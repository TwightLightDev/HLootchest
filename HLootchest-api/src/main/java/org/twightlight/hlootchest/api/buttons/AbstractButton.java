package org.twightlight.hlootchest.api.buttons;

import org.twightlight.hlootchest.utils.Utility;
import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.exp4j.Expression;
import org.twightlight.libs.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.twightlight.hlootchest.api.HLootChest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.events.lootchest.ButtonSpawnEvent;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.objects.ButtonSound;
import org.twightlight.hlootchest.objects.Icon;
import org.twightlight.hlootchest.scheduler.SchedulerAdapter;
import org.twightlight.hlootchest.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractButton implements TButton {

    public static final ConcurrentHashMap<Integer, TButton> buttonIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Player, List<TButton>> playerButtonMap = new ConcurrentHashMap<>();

    protected final Player owner;
    protected final TYamlWrapper config;
    protected final String pathToButton;
    protected final boolean isPreview;
    protected final ButtonConfig cfg;
    protected final NMSBridge nms;
    protected final HLootChest apiInstance;
    protected final NMSHandler handler;

    protected ArmorStand armorstand;
    protected int id;
    protected boolean isMoved = false;
    protected ScheduledTask task;
    protected boolean clickable = false;
    protected boolean moveable = true;
    protected ButtonType type;
    protected boolean isHiding = false;
    protected TIcon icon = null;
    protected boolean removed = false;

    protected final List<ArmorStand> childStands = new ArrayList<>();
    protected final Map<ArmorStand, String> childNameModes = new HashMap<>();
    protected final Map<ArmorStand, TIcon> childIcons = new HashMap<>();

    public AbstractButton(Location location, ButtonType type, Player player, String path,
                          TYamlWrapper config, boolean isPreview,
                          NMSBridge nms, HLootChest apiInstance, NMSHandler handler) {
        this.owner = player;
        this.config = config;
        this.pathToButton = path;
        this.isPreview = isPreview;
        this.nms = nms;
        this.apiInstance = apiInstance;
        this.handler = handler;
        this.type = type;

        boolean betterCheck = apiInstance.getConfigUtil().getMainConfig()
                .getBoolean("performance.modern-check-algorithm", false);
        this.cfg = ButtonConfigParser.parse(config, path, betterCheck);

        ButtonSpawnEvent event = new ButtonSpawnEvent(owner, this);
        apiInstance.getScheduler().runTask(owner, () -> Bukkit.getPluginManager().callEvent(event));

        initializeArmorStand(location);
        setupNameRefresh();
        setupRotationOnSpawn(location);
        setupIconHandling();
        setupChildButtons(location);
        setupMovementBehavior();
        unmovablePeriod(250);
    }

    private void initializeArmorStand(Location location) {
        String name = cfg.displayNames.get(0);
        armorstand = nms.createArmorStand(location,
                apiInstance.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&', name)),
                cfg.isSmall, cfg.enableName, location.getYaw(), location.getPitch());
        this.id = armorstand.getEntityId();
        nms.rotate(armorstand, config, pathToButton);
        buttonIdMap.put(this.id, this);
        playerButtonMap.computeIfAbsent(owner, k -> new ArrayList<>()).add(this);
        nms.sendSpawnPacket(owner, armorstand);
    }

    private void setupNameRefresh() {
        if (cfg.nameRefreshInterval <= 0) return;
        final int[] i = {1};
        apiInstance.getScheduler().runTaskTimer(owner, () -> {
            if (!owner.isOnline() || removed) return;
            if (!cfg.dynamicName) {
                nms.setCustomName(armorstand, ChatColor.translateAlternateColorCodes('&',
                        apiInstance.getLanguageUtil().p(owner, cfg.displayNames.get(0))));
            } else {
                if (i[0] >= cfg.displayNames.size()) i[0] = 0;
                nms.setCustomName(armorstand, ChatColor.translateAlternateColorCodes('&',
                        apiInstance.getLanguageUtil().p(owner, cfg.displayNames.get(i[0]))));
                i[0]++;
            }
            nms.sendMetadataPacket(owner, armorstand);
        }, 0L, cfg.nameRefreshInterval);
    }

    private void setupRotationOnSpawn(Location location) {
        if (!cfg.rotateOnSpawn) return;
        float angle = (float) (location.getYaw() - cfg.rotateOnSpawnFinalYaw);
        int multiply = cfg.rotateReverse ? 1 : -1;
        final int[] i = {0};
        apiInstance.getScheduler().runTaskTimer(owner, () -> {
            if (!owner.isOnline() || i[0] >= 1) return;
            i[0]++;
            nms.spin(owner, armorstand, location.getYaw() - (angle * i[0] * multiply));
        }, 2L, 1L);
    }

    private void setupIconHandling() {
        if (cfg.holdingIcon && !cfg.dynamicIcon) {
            ItemStack item = handler.createItem(
                    XMaterial.valueOf(cfg.iconMaterial).get(),
                    cfg.iconHeadValue, cfg.iconData, "", new ArrayList<>(), cfg.iconGlowing);
            TIcon ticon = new Icon(item, cfg.iconSlot);
            nms.equipIcon(owner, armorstand, ticon.getItemStack(), ticon.getItemSlot());
            this.icon = ticon;
        }
        if (cfg.holdingIcon && cfg.dynamicIcon && cfg.dynamicIcons != null && cfg.iconRefreshInterval > 0) {
            List<TIcon> icons = buildIconList(cfg.dynamicIcons);
            final int[] i = {0};
            apiInstance.getScheduler().runTaskTimer(owner, () -> {
                if (!owner.isOnline() || removed) return;
                if (i[0] >= icons.size()) i[0] = 0;
                if (!isHiding) {
                    nms.equipIcon(owner, armorstand, icons.get(i[0]).getItemStack(), icons.get(i[0]).getItemSlot());
                    setIcon(icons.get(i[0]));
                }
                i[0]++;
            }, 0L, cfg.iconRefreshInterval);
        }
    }

    private void setupChildButtons(Location parentLocation) {
        for (ChildConfig ch : cfg.children) {
            if (!apiInstance.getPlayerUtil().checkConditions(owner, config, ch.path + ".spawn-requirements")) continue;
            Location childLoc = resolveChildLocation(parentLocation, ch);
            if (childLoc == null) continue;
            spawnChild(ch, childLoc);
        }
    }

    private Location resolveChildLocation(Location parent, ChildConfig ch) {
        if (ch.locationRaw != null) return Utility.stringToLocation(ch.locationRaw);
        if (ch.locationOffsetRaw != null) return calculateOffset(parent, ch.locationOffsetRaw);
        return null;
    }

    private Location calculateOffset(Location parent, String offsetStr) {
        String[] parts = offsetStr.split(",");
        double yaw = nms.getYaw(armorstand);
        Vector vector = nms.getBukkitLocation(armorstand).getDirection().normalize();
        if (parts.length == 3) {
            return parent.clone().add(
                    buildExpr(parts[0], yaw, vector).evaluate(),
                    buildExpr(parts[1], yaw, vector).evaluate(),
                    buildExpr(parts[2], yaw, vector).evaluate());
        }
        if (parts.length == 5) {
            Location loc = parent.clone();
            loc.setYaw(parent.getYaw() + (float) new ExpressionBuilder(parts[3]).build().evaluate());
            loc.setPitch(parent.getPitch() + (float) new ExpressionBuilder(parts[4]).build().evaluate());
            return loc.add(
                    buildExpr(parts[0], yaw, vector).evaluate(),
                    buildExpr(parts[1], yaw, vector).evaluate(),
                    buildExpr(parts[2], yaw, vector).evaluate());
        }
        return null;
    }

    private Expression buildExpr(String expr, double yaw, Vector vector) {
        return new ExpressionBuilder(expr)
                .variables("yaw", "VectorX", "VectorZ").build()
                .setVariable("yaw", Math.toRadians(yaw))
                .setVariable("VectorX", vector.getX())
                .setVariable("VectorZ", vector.getZ());
    }

    private void spawnChild(ChildConfig ch, Location loc) {
        String name = ch.displayNames.get(0);
        ArmorStand child = nms.createArmorStand(loc,
                apiInstance.getLanguageUtil().p(owner, ChatColor.translateAlternateColorCodes('&', name)),
                ch.isSmall, ch.enableName, loc.getYaw(), loc.getPitch());
        nms.rotate(child, config, ch.path);
        childStands.add(child);
        childNameModes.put(child, ch.nameVisibleMode);
        nms.sendSpawnPacket(owner, child);

        if (ch.nameRefreshInterval > 0) {
            scheduleChildNameRefresh(child, ch);
        }
        if (ch.iconMaterial != null) {
            setupChildIcon(child, ch);
        }
    }

    private void scheduleChildNameRefresh(ArmorStand child, ChildConfig ch) {
        final int[] i = {1};
        apiInstance.getScheduler().runTaskTimer(owner, () -> {
            if (!owner.isOnline() || removed) return;
            if (!ch.dynamicName) {
                nms.setCustomName(child, apiInstance.getLanguageUtil().p(owner,
                        ChatColor.translateAlternateColorCodes('&', ch.displayNames.get(0))));
            } else {
                if (i[0] >= ch.displayNames.size()) i[0] = 0;
                nms.setCustomName(child, apiInstance.getLanguageUtil().p(owner,
                        ChatColor.translateAlternateColorCodes('&', ch.displayNames.get(i[0]))));
                i[0]++;
            }
            nms.sendMetadataPacket(owner, child);
        }, 0L, ch.nameRefreshInterval);
    }

    private void setupChildIcon(ArmorStand child, ChildConfig ch) {
        if (!ch.hasDynamicIcon) {
            ItemStack item = handler.createItem(
                    XMaterial.valueOf(ch.iconMaterial).get(),
                    ch.iconHeadValue, ch.iconData, "", new ArrayList<>(), ch.iconGlowing);
            TIcon ticon = new Icon(item, ch.iconSlot);
            nms.equipIcon(owner, child, ticon.getItemStack(), ticon.getItemSlot());
            childIcons.put(child, ticon);
        } else if (ch.dynamicIcons != null) {
            List<TIcon> icons = buildIconList(ch.dynamicIcons);
            nms.equipIcon(owner, child, icons.get(0).getItemStack(), icons.get(0).getItemSlot());
            childIcons.put(child, icons.get(0));
            if (ch.iconRefreshInterval > 0) {
                final int[] i = {1};
                apiInstance.getScheduler().runTaskTimer(owner, () -> {
                    if (!owner.isOnline() || removed) return;
                    if (i[0] >= icons.size()) i[0] = 0;
                    if (!isHiding) {
                        nms.equipIcon(owner, child, icons.get(i[0]).getItemStack(), icons.get(i[0]).getItemSlot());
                        childIcons.put(child, icons.get(i[0]));
                    }
                    i[0]++;
                }, 0L, ch.iconRefreshInterval);
            }
        }
    }

    private List<TIcon> buildIconList(List<ButtonConfig.DynamicIconEntry> entries) {
        List<TIcon> icons = new ArrayList<>();
        for (ButtonConfig.DynamicIconEntry e : entries) {
            ItemStack item = handler.createItem(
                    XMaterial.valueOf(e.material).get(),
                    e.headValue, e.data, "", new ArrayList<>(), e.glowing);
            icons.add(new Icon(item, e.slot));
        }
        return icons;
    }

    private void setupMovementBehavior() {
        if (!cfg.moveForward) { this.task = null; return; }
        this.task = apiInstance.getScheduler().runTaskTimer(owner, () -> {
            if (owner == null || armorstand == null || !owner.isOnline()) { cancelTask(); return; }
            Location standLoc = nms.getBukkitLocation(armorstand);
            if (owner.getLocation().distance(standLoc) > 5 || isHiding) {
                if (isMoved) resetButtonPosition();
                return;
            }
            boolean looking = GazeDetector.isLooking(owner,
                    nms.getX(armorstand), nms.getY(armorstand), nms.getZ(armorstand),
                    cfg.betterCheckAlgorithm);
            if (looking && !isMoved && moveable) {
                doMoveForward();
            } else if (!looking && isMoved && moveable) {
                resetButtonPosition();
            }
        }, 0L, 5L);
    }

    private void doMoveForward() {
        moveForward();
        if (cfg.hoverSound != null) {
            handler.playSound(owner, owner.getLocation(),
                    cfg.hoverSound.getSoundString(), cfg.hoverSound.getYaw(), cfg.hoverSound.getPitch());
        }
        updateNameVisibility(true);
        unmovablePeriod(500);
        isMoved = true;
    }

    private void resetButtonPosition() {
        moveBackward();
        updateNameVisibility(false);
        unmovablePeriod(500);
        isMoved = false;
    }

    private void updateNameVisibility(boolean visible) {
        if (cfg.nameVisibleMode.equals("hover")) {
            nms.setNameVisible(armorstand, visible);
            nms.sendMetadataPacket(owner, armorstand);
        }
        for (ArmorStand stand : childStands) {
            String mode = childNameModes.get(stand);
            if (mode != null && mode.equals("hover")) {
                nms.setNameVisible(stand, visible);
                nms.sendMetadataPacket(owner, stand);
            }
        }
    }

    private void unmovablePeriod(int timeMillis) {
        this.moveable = false;
        long ticks = Math.max(1, timeMillis / 50);
        apiInstance.getScheduler().runTaskLater(owner, () -> moveable = true, ticks);
    }


    @Override
    public void equipIcon(TIcon icon) {
        nms.equipIcon(owner, armorstand, icon.getItemStack(), icon.getItemSlot());
    }

    @Override
    public void hide(boolean isHiding) {
        this.isHiding = isHiding;
        if (isHiding) {
            nms.sendDespawnPacket(owner, armorstand);
            for (ArmorStand stand : childStands) nms.sendDespawnPacket(owner, stand);
        } else {
            nms.sendSpawnPacket(owner, armorstand);
            if (icon != null) nms.equipIcon(owner, armorstand, icon.getItemStack(), icon.getItemSlot());
            for (ArmorStand stand : childStands) {
                nms.sendSpawnPacket(owner, stand);
                TIcon ci = childIcons.get(stand);
                if (ci != null) nms.equipIcon(owner, stand, ci.getItemStack(), ci.getItemSlot());
            }
        }
    }

    @Override
    public void remove() {
        if (armorstand != null) {
            removed = true;
            cancelTask();
            nms.sendDespawnPacket(owner, armorstand);
            for (ArmorStand stand : childStands) {
                nms.sendDespawnPacket(owner, stand);
                childIcons.remove(stand);
            }
            buttonIdMap.remove(id);
            childStands.clear();
            childNameModes.clear();
            List<TButton> buttons = playerButtonMap.get(owner);
            if (buttons != null) {
                buttons.remove(this);
                playerButtonMap.put(owner, buttons);
            }
        }
    }

    @Override
    public void moveForward() {
        clickable = true;
        nms.moveForward(owner, armorstand, 0.5f);
        for (ArmorStand stand : childStands) nms.moveForward(owner, stand, 0.5f);
    }

    @Override
    public void moveBackward() {
        clickable = false;
        nms.moveBackward(owner, armorstand, 0.5f);
        for (ArmorStand stand : childStands) nms.moveBackward(owner, stand, 0.5f);
    }

    public void cancelTask() { if (this.task != null) this.task.cancel(); }

    @Override public int getCustomId() { return id; }
    @Override public Player getOwner() { return owner; }
    @Override public boolean isMoved() { return isMoved; }
    @Override public boolean isClickable() { return clickable; }
    @Override public void setClickable(boolean bool) { this.clickable = bool; }
    @Override public ButtonType getType() { return type; }
    @Override public List<String> getActions() { return cfg.actions; }
    @Override public ButtonSound getSound() { return cfg.clickSound; }
    @Override public boolean isHiding() { return isHiding; }
    @Override public void setIcon(TIcon icon) { this.icon = icon; }
    @Override public TYamlWrapper getConfig() { return config; }
    @Override public ScheduledTask getTask() { return task; }
    @Override public boolean isMoveable() { return moveable; }
    @Override public void setMoveable(boolean moveable) { this.moveable = moveable; }
    @Override public void setType(ButtonType type) { this.type = type; }
    @Override public void setActions(List<String> actions) { cfg.actions = actions; }
    @Override public void setSound(ButtonSound sound) { cfg.clickSound = sound; }
    @Override public void setHidingState(boolean hiding) { isHiding = hiding; }
    @Override public TIcon getIcon() { return icon; }
    @Override public void setConfig(TYamlWrapper config) { }
    @Override public String getPathToButton() { return pathToButton; }
    @Override public void setPathToButton(String pathToButton) { }
    @Override public boolean isDynamicName() { return cfg.dynamicName; }
    @Override public boolean isDynamicIcon() { return cfg.dynamicIcon; }
    @Override public String getNameVisibleMode() { return cfg.nameVisibleMode; }
    @Override public boolean isPreview() { return isPreview; }
}

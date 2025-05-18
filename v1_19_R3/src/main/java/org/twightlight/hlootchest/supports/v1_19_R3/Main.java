package org.twightlight.hlootchest.supports.v1_19_R3;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.enums.ProtocolVersion;
import org.twightlight.hlootchest.api.interfaces.functional.LootChestFactory;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.internal.TConfigManager;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_19_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_19_R3.listeners.ClickEvent;
import org.twightlight.hlootchest.supports.v1_19_R3.supports.PacketEventsSupport;
import org.twightlight.hlootchest.supports.v1_19_R3.utilities.NMSUtil;
import org.twightlight.hlootchest.utils.ColorUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends NMSHandler {
    public static NMSHandler handler;
    public static ColorUtils colorUtils;
    public static HLootchest api;
    public static NMSUtil nmsUtil;
    private static final Map<String, LootChestFactory> tboxdata = new HashMap<>();
    private static final Map<String, LootChestFactory> effectsList = new HashMap<>();
    private static boolean haspacketService = false;
    private static PacketEventsSupport packetService;

    public Main(Plugin pl, String name, HLootchest api) {
        super(pl, name);
        handler = this;
        colorUtils = new ColorUtils();
        this.api = api;
        nmsUtil = new NMSUtil();
        Bukkit.getServer().getPluginManager().registerEvents(new ClickEvent(), pl);

        if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
            haspacketService = true;
            packetService = new PacketEventsSupport();
        }
    }

    public static boolean hasPacketService() {
        return haspacketService;
    }

    public static PacketEventsSupport getPacketService() {
        return packetService;
    }

    @Override
    public NMSService getNMSService() {
        return nmsUtil;
    }

    public static String p(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return colorUtils.colorize(value);
        return colorUtils.colorize(PlaceholderAPI.setPlaceholders(p, value));
    }

    public static String replaceCommand(Player p, String command) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return command.replace("{player}", p.getName());
        return PlaceholderAPI.setPlaceholders(p, command);
    }

    public void registerButtonClick(Player player) {
    }

    public TButton spawnButton(Location location, ButtonType type, Player player, String path, TConfigManager config) {
        return new Button(location, type, player, path, config, false);
    }

    public TButton spawnPreviewButton(Location location, ButtonType type, Player player, String path, TConfigManager config) {
        return new Button(location, type, player, path, config, true);
    }

    public TBox spawnBox(Location location, String boxid, Player player, ItemStack icon, TConfigManager config) {
        LootChestFactory factory = tboxdata.get(boxid);
        if (factory == null) {
            player.sendMessage(ChatColor.RED + "Unknow lootchest type!");
            return null;
        }
        return factory.create(location, player, icon, config, boxid);
    }

    public void removeButtonsFromPlayer(Player player, ButtonType type) {
        try {
            if (getGlobalButtons().get(player) == null)
                return;
            List<TButton> needRemove = new ArrayList<>();
            int times = (getGlobalButtons().get(player)).size();
            for (int i = 0; i < times; i++) {
                if (((getGlobalButtons().get(player)).get(i)).getType() == type)
                    needRemove.add((getGlobalButtons().get(player)).get(i));
            }
            for (TButton tButton : needRemove)
                tButton.remove();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void hideButtonsFromPlayer(Player player, ButtonType type, boolean state) {
        try {
            if (getGlobalButtons().get(player) == null)
                return;
            int times = (getGlobalButtons().get(player)).size();
            for (int i = 0; i < times; i++) {
                if (((getGlobalButtons().get(player)).get(i)).getType() == type)
                    ((getGlobalButtons().get(player)).get(i)).hide(state);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ConcurrentHashMap<Player, List<TButton>> getGlobalButtons() {
        return Button.playerButtonMap;
    }

    public TButton getButtonFromId(int id) {
        return Button.buttonIdMap.get(Integer.valueOf(id));
    }

    public TBox getBoxFromPlayer(Player player) {
        return (TBox)BoxManager.boxPlayerlists.getOrDefault(player, null);
    }

    public void playSound(Player player, Location location, String sound, float yaw, float pitch) {
        player.playSound(location, XSound.of(sound).get().get(), yaw, pitch);
    }

    public ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            Material Xmaterial = XMaterial.valueOf(material).get();
            if (Xmaterial == null)
                return XMaterial.BEDROCK.parseItem();
            i = new ItemStack(Xmaterial, amount);
        } catch (Exception ex) {
            i = XMaterial.BEDROCK.parseItem();
        }
        return i;
    }

    public ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = handler.createItemStack(XMaterial.matchXMaterial(material).name(), 1, (short) data);
        if (i == null)
            return null;

        ItemMeta itemMeta = i.getItemMeta();
        if (itemMeta == null)
            return null;

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            itemMeta.setLore(coloredLore);
        }
        if (enchanted) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(itemMeta);

        if (material == XMaterial.PLAYER_HEAD.get() && headUrl != null && !headUrl.trim().isEmpty()) {
            try {
                SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                if (api.getSupportsUtil().hasHeadDatabase()) {
                    if (headUrl.contains(":")) {
                        String[] value = headUrl.split(":", 2);
                        if (value[0].equals("hdb")) {
                            headUrl = api.getSupportsUtil().getHeadDatabaseService().getBase64(value[1]);
                        }
                    }
                }
                if (isValidBase64(headUrl)) {
                    profile.getProperties().put("textures", new Property("textures", headUrl));

                    Field field = skullMeta.getClass().getDeclaredField("profile");
                    field.setAccessible(true);
                    field.set(skullMeta, profile);
                    i.setItemMeta(skullMeta);
                } else {
                    Bukkit.getLogger().warning("Invalid head URL: " + headUrl);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return i;
    }

    public static boolean isValidBase64(String base64) {
        String base64Pattern = "^[A-Za-z0-9+/]+={0,2}$";
        return base64 != null && base64.length() % 4 == 0 && Pattern.matches(base64Pattern, base64);
    }

    public void register(String boxid, LootChestFactory function) {
        if (tboxdata.containsKey(boxid))
            throw new IllegalArgumentException("LootChest with id " + boxid + " is already registered!");
        tboxdata.put(boxid, function);
    }

    public void deregister(String boxid) {
        if (!tboxdata.containsKey(boxid)) {
            throw new IllegalArgumentException("LootChest with id " + boxid + " not found!");
        }
        tboxdata.remove(boxid);
    }

    public void registerAnimation(String effectid, LootChestFactory function) {
        if (effectsList.containsKey(effectid)) {
            throw new IllegalArgumentException("Effect with id " + effectid + " is already registered!");
        }
        effectsList.put(effectid, function);
    }

    public Map<String, LootChestFactory> getAnimationsRegistrationData() {
        return effectsList;
    }


    public Map<String, LootChestFactory> getRegistrationData() {
        return tboxdata;
    }

    public static EulerAngle stringToVector3f(String str) {
        try {
            String[] parts = str.split(",");
            if (parts.length != 3)
                throw new IllegalArgumentException("Invalid vector string: " + str);
            float x = Float.parseFloat(parts[0]);
            float y = Float.parseFloat(parts[1]);
            float z = Float.parseFloat(parts[2]);
            return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
        } catch (Exception e) {
            System.err.println("There is some errors during deserialize this line: " + str);
            throw new RuntimeException(e);
        }

    }

    public Location stringToLocation(String locString) {
        try {
            String[] parts = locString.split(",");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Invalid location string: " + locString);
            }

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                throw new IllegalArgumentException("World not found: " + parts[0]);
            }

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            System.err.println("There is some errors during deserialize this line: " + locString);
            throw new RuntimeException(e);
        }
    }

    public static void rotate(ArmorStand armorStand, TConfigManager config, String path) {
        if (config.getYml().getConfigurationSection(path + ".rotations") != null) {
            Set<String> rotations = config.getYml().getConfigurationSection(path + ".rotations").getKeys(false);
            for (String s : rotations) {
                String rotationString = config.getString(path + ".rotations." + s + ".value");
                EulerAngle rotation = stringToVector3f(rotationString);
                String position = config.getString(path + ".rotations." + s + ".position");
                switch (position) {
                    case "HEAD":
                        armorStand.setHeadPose(rotation);
                    case "BODY":
                        armorStand.setBodyPose(rotation);
                    case "RIGHT_ARM":
                        armorStand.setRightArmPose(rotation);
                    case "LEFT_ARM":
                        armorStand.setLeftLegPose(rotation);
                    case "RIGHT_LEG":
                        armorStand.setRightLegPose(rotation);
                    case "LEFT_LEG":
                        armorStand.setLeftLegPose(rotation);
                }
            }
        }
    }

    public void setFakeGameMode(Player p, GameMode gamemode) {
        if (hasPacketService()) {
            getPacketService().setGameMode(p, gamemode);
        }
    }

    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.v1_19_R3;
    }
}

package org.twightlight.hlootchest.supports.v1_19_R3;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
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
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.LootChestFactory;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_19_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_19_R3.listeners.ClickEvent;
import org.twightlight.hlootchest.utils.ColorUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main extends NMSHandler {
    public static NMSHandler handler;
    public static ColorUtils colorUtils;
    public static HLootchest api;
    private static final Map<String, LootChestFactory> tboxdata = new HashMap<>();

    public Main(Plugin pl, String name, HLootchest api) {
        super(pl, name);
        handler = this;
        colorUtils = new ColorUtils();
        this.api = api;
        Bukkit.getServer().getPluginManager().registerEvents(new ClickEvent(), pl);

    }

    public static String p(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return colorUtils.colorize(value);
        return colorUtils.colorize(PlaceholderAPI.setPlaceholders(p, value));
    }

    public void registerButtonClick(Player player) {
    }

    public void spawnButton(Location location, ButtonType type, Player player, ItemStack icon, String path, TConfigManager config) {
        new Button(location, type, player, icon, path, config);
    }

    public TBox spawnBox(Location location, String boxid, Player player, ItemStack icon, TConfigManager config, Location initialLocation) {
        LootChestFactory factory = tboxdata.get(boxid);
        if (factory == null) {
            player.sendMessage(ChatColor.RED + "Unknow lootchest type!");
            return null;
        }
        return factory.create(location, player, icon, config, boxid, initialLocation);
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
        player.playSound(location,
                XSound.valueOf(sound).parseSound(), yaw, pitch);
    }

    public ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            Material Xmaterial = XMaterial.valueOf(material).parseMaterial();
            if (Xmaterial == null)
                return XMaterial.BEDROCK.parseItem();
            i = new ItemStack(Xmaterial, amount);
        } catch (Exception ex) {
            i = XMaterial.BEDROCK.parseItem();
        }
        return i;
    }

    public ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = handler.createItemStack(XMaterial.matchXMaterial(material).name(), 1, (short)data);
        ItemMeta itemMeta = i.getItemMeta();
        itemMeta.setDisplayName(colorUtils.colorize(displayName));
        if (!lore.isEmpty()) {
            lore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            itemMeta.setLore(lore);
        }
        if (enchanted)
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS });
        itemMeta.setCustomModelData(data);
        i.setItemMeta(itemMeta);
        if (material.equals(XMaterial.PLAYER_HEAD.parseMaterial()) &&
                headUrl != null) {
            SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", headUrl));
            try {
                Field field = skullMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(skullMeta, profile);
            } catch (IllegalArgumentException | NoSuchFieldException | SecurityException |
                     IllegalAccessException exception) {
                return null;
            }
            i.setItemMeta(skullMeta);
        }
        return i;
    }

    public void register(String boxid, LootChestFactory function) {
        if (tboxdata.containsKey(boxid))
            throw new IllegalArgumentException("LootChest with id " + boxid + " is already registered!");
        tboxdata.put(boxid, function);
    }

    public Map<String, LootChestFactory> getRegistrationData() {
        return tboxdata;
    }

    public static EulerAngle stringToVector3f(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3)
            throw new IllegalArgumentException("Invalid vector string: " + str);
        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public Location stringToLocation(String locString) {
        String[] parts = locString.split(",");
        if (parts.length < 4)
            throw new IllegalArgumentException("Invalid location string: " + locString);
        World world = Bukkit.getWorld(parts[0]);
        if (world == null)
            throw new IllegalArgumentException("World not found: " + parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = (parts.length > 4) ? Float.parseFloat(parts[4]) : 0.0F;
        float pitch = (parts.length > 5) ? Float.parseFloat(parts[5]) : 0.0F;
        return new Location(world, x, y, z, yaw, pitch);
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
        PacketPlayOutGameStateChange packet = null;
        switch (gamemode) {
            case SURVIVAL:
                packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 0.0F);
                break;
            case CREATIVE:
                packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 1.0F);
                break;
            case ADVENTURE:
                packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 2.0F);
                break;
            case SPECTATOR:
                packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 3.0F);
                break;
        }
        if (packet != null)
            (((CraftPlayer)p).getHandle()).b.a(packet);
    }
}

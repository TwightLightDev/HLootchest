package org.twightlight.hlootchest.supports.v1_8_R3;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_8_R3.Vector3f;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.LootChestFactory;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_8_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_8_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_8_R3.listeners.ClickEvent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends NMSHandler {

    public static NMSHandler handler;
    public static HLootchest api;
    private static final Map<String, LootChestFactory> tboxdata = new HashMap<>();

    public Main(Plugin pl, String name, HLootchest api) {
        super(pl, name);
        handler = this;
        this.api = api;
    }

    public static String p(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return ChatColor.translateAlternateColorCodes('&', value);
        return ChatColor.translateAlternateColorCodes('&', (PlaceholderAPI.setPlaceholders(p, value)));
    }

    public void registerButtonClick(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection = new ClickEvent(nmsPlayer.playerConnection.networkManager, nmsPlayer);
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
            if (getGlobalButtons().get(player) == null) {
                return;
            }
            List<TButton> needRemove = new ArrayList<>();
            int times = getGlobalButtons().get(player).size();
            for (int i = 0; i < times; i++) {
                if (getGlobalButtons().get(player).get(i).getType() == type) {
                    needRemove.add(getGlobalButtons().get(player).get(i));
                }
            }

            for (TButton tButton : needRemove) {
                tButton.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideButtonsFromPlayer(Player player, ButtonType type, boolean state) {
        try {
            if (getGlobalButtons().get(player) == null) {
                return;
            }
            int times = getGlobalButtons().get(player).size();
            for (int i = 0; i < times; i++) {
                if (getGlobalButtons().get(player).get(i).getType() == type) {
                    getGlobalButtons().get(player).get(i).hide(state);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Player, List<TButton>> getGlobalButtons() {
        return Button.playerButtonMap;
    }

    public TButton getButtonFromId(int id) {
        return Button.buttonIdMap.get(id);
    }

    public TBox getBoxFromPlayer(Player player) {
        return BoxManager.boxPlayerlists.getOrDefault(player, null);
    }

    public ItemStack createItemStack(String material, int amount, short data) {
        ItemStack i;
        try {
            Material Xmaterial = XMaterial.valueOf(material).parseMaterial();
            if (Xmaterial == null) {
                return XMaterial.BEDROCK.parseItem();
            }
            i = new ItemStack(Xmaterial, amount, data);
        } catch (Exception ex) {
            i = XMaterial.BEDROCK.parseItem();
        }
        return i;
    }

    public void playSound(Player player, Location location, String sound, float yaw, float pitch) {
        player.playSound(location,
                XSound.valueOf(sound).parseSound(),
                yaw,
                pitch);
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

        if (material == XMaterial.PLAYER_HEAD.parseMaterial() && headUrl != null && !headUrl.trim().isEmpty()) {
            try {
                SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);

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
        if (tboxdata.containsKey(boxid)) {
            throw new IllegalArgumentException("LootChest with id " + boxid + " is already registered!");
        }
        tboxdata.put(boxid, function);
    }

    public Map<String, LootChestFactory> getRegistrationData() {
        return tboxdata;
    }


    public static Vector3f stringToVector3f(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid vector string: " + str);
        }
        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        return new Vector3f(x, y, z);
    }

    public Location stringToLocation(String locString) {
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
    }

    public static void rotate(EntityArmorStand armorStand, TConfigManager config, String path) {
        if (config.getYml().getConfigurationSection(path+".rotations") != null) {
            Set<String> rotations = config.getYml().getConfigurationSection(path + ".rotations").getKeys(false);
            for (String s : rotations) {
                Vector3f rotation = Main.stringToVector3f(config.getString(path + ".rotations" + "." + s + ".value"));
                String position = config.getString(path + ".rotations" + "." + s + ".position");
                switch (position) {
                    case "HEAD":
                        armorStand.setHeadPose(rotation);
                        break;
                    case "BODY":
                        armorStand.setBodyPose(rotation);
                        break;
                    case "RIGHT_ARM":
                        armorStand.setRightArmPose(rotation);
                        break;
                    case "LEFT_ARM":
                        armorStand.setLeftArmPose(rotation);
                        break;
                    case "RIGHT_LEG":
                        armorStand.setRightLegPose(rotation);
                        break;
                    case "LEFT_LEG":
                        armorStand.setLeftLegPose(rotation);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void setFakeGameMode(Player p, GameMode gamemode) {
        switch (gamemode) {
            case SURVIVAL:
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 0));
                break;
            case CREATIVE:
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 1));
                break;
            case ADVENTURE:
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 2));
                break;
            case SPECTATOR:
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 3));
                break;
        }
    }
}

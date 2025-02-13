package org.twightlight.hlootchest.supports.v1_19_R3;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.core.Vector3f;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TButton;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.api.supports.LootChestFactory;
import org.twightlight.hlootchest.api.supports.NMSHandler;
import org.twightlight.hlootchest.supports.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.v1_19_R3.buttons.Button;
import org.twightlight.hlootchest.supports.v1_19_R3.listeners.ClickEvent;

public class Main extends NMSHandler {
    public static NMSHandler handler;

    private static final Map<String, LootChestFactory> tboxdata = new HashMap<>();

    public Main(Plugin pl, String name) {
        super(pl, name);
        handler = this;
    }

    public static String p(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return ChatColor.translateAlternateColorCodes('&', value);
        return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, value));
    }

    public void registerButtonClick(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        nmsPlayer.b = (PlayerConnection)new ClickEvent(getPlayerConnection(nmsPlayer), nmsPlayer);
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
            int times = ((List)getGlobalButtons().get(player)).size();
            for (int i = 0; i < times; i++) {
                if (((TButton)((List<TButton>)getGlobalButtons().get(player)).get(i)).getType() == type)
                    needRemove.add(((List<TButton>)getGlobalButtons().get(player)).get(i));
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
            int times = ((List)getGlobalButtons().get(player)).size();
            for (int i = 0; i < times; i++) {
                if (((TButton)((List<TButton>)getGlobalButtons().get(player)).get(i)).getType() == type)
                    ((TButton)((List<TButton>)getGlobalButtons().get(player)).get(i)).hide(state);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ConcurrentHashMap<Player, List<TButton>> getGlobalButtons() {
        return Button.playerButtonMap;
    }

    public TButton getButtonFromId(int id) {
        return (TButton)Button.buttonIdMap.get(Integer.valueOf(id));
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
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (!lore.isEmpty())
            itemMeta.setLore(lore);
        if (enchanted)
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS });
        itemMeta.setCustomModelData(data);
        i.setItemMeta(itemMeta);
        if (material.equals(XMaterial.PLAYER_HEAD.parseMaterial()) && headUrl != null) {
            SkullMeta skullMeta = (SkullMeta)i.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", headUrl));
            try {
                Field field = skullMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(skullMeta, profile);
            } catch (IllegalArgumentException|NoSuchFieldException|SecurityException|IllegalAccessException exception) {
                return null;
            }
            i.setItemMeta((ItemMeta)skullMeta);
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

    public static Vector3f stringToVector3f(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3)
            throw new IllegalArgumentException("Invalid vector string: " + str);
        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        return new Vector3f(x, y, z);
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

    public static void rotate(EntityArmorStand armorStand, TConfigManager config, String path) {
        if (config.getYml().getConfigurationSection(path + ".rotations") != null) {
            Set<String> rotations = config.getYml().getConfigurationSection(path + ".rotations").getKeys(false);
            for (String s : rotations) {
                String rotationString = config.getString(path + ".rotations." + s + ".value");
                Vector3f rotation = stringToVector3f(rotationString);
                String position = config.getString(path + ".rotations." + s + ".position");
                switch (position) {
                    case "HEAD":
                        armorStand.a(rotation);
                    case "BODY":
                        armorStand.b(rotation);
                    case "RIGHT_ARM":
                        armorStand.d(rotation);
                    case "LEFT_ARM":
                        armorStand.c(rotation);
                    case "RIGHT_LEG":
                        armorStand.f(rotation);
                    case "LEFT_LEG":
                        armorStand.e(rotation);
                }
            }
        }
    }

    public static NetworkManager getPlayerConnection(EntityPlayer player) {
        try {
            Field connectionField = PlayerConnection.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            return (NetworkManager)connectionField.get(player.b);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            e.printStackTrace();
            return null;
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
            (((CraftPlayer)p).getHandle()).b.a((Packet)packet);
    }
}

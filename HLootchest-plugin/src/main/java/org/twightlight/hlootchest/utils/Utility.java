package org.twightlight.hlootchest.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.twightlight.hlootchest.HLootchest;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Utility {
    public static void info(String message) {
        System.out.println("[HLootchest] " + message);
    }
    public static void error(String message) {
        System.err.println("[HLootchest] " + message);
    }


    public static String p(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return c(value);
        return c(PlaceholderAPI.setPlaceholders(p, value));
    }

    public static List<String> p(Player p, List<String> value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return value.stream().map(Utility::c).collect(Collectors.toList());
        return value.stream().map(s -> c(PlaceholderAPI.setPlaceholders(p, s))).collect(Collectors.toList());
    }

    public static String getMsg(Player p, String path) {
        return p(p, HLootchest.messagesConfig.getString("Messages." + path).replace("{prefix}", HLootchest.messagesConfig.getString("Messages.prefix")));
    }

    public static String c(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = HLootchest.getNms().createItemStack(String.valueOf(material), 1, (short) data);
        ItemMeta itemMeta = i.getItemMeta();
        itemMeta.setDisplayName(c(displayName));
        if (!lore.isEmpty()) {
            itemMeta.setLore(lore);
        }
        if (enchanted) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(itemMeta);
        if (material.equals(Material.SKULL_ITEM) &&
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
                exception.printStackTrace();
                return null;
            }
            i.setItemMeta(skullMeta);
        }
        return i;
    }
    public static Location stringToLocation(String locString) {
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
    public static String locationToString(Location loc) {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch());
    }

    public static <T> Set<T> getRandomElements(Set<T> set, int n) {
        List<T> list = new ArrayList<>(set);
        Set<T> randomElements = new HashSet<>();
        Random random = new Random();

        if (n > list.size()) {
            throw new IllegalArgumentException("Cannot get more unique elements than the size of the set.");
        }

        while (randomElements.size() < n) {
            T element = list.get(random.nextInt(list.size()));
            randomElements.add(element);
        }

        return randomElements;
    }
}


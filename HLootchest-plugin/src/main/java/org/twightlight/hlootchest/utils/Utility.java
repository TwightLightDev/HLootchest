package org.twightlight.hlootchest.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.objects.TConfigManager;

import java.util.*;

public class Utility {
    public static void info(String message) {
        System.out.println(message);
    }
    public static void error(String message) {
        System.err.println(message);
    }


    public static String pC(Player p, String value) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return c(value);
        return c(PlaceholderAPI.setPlaceholders(p, value));
    }

    public static String p(Player p, String value) {
        if (HLootchest.hex_gradient) {
            return HLootchest.colorUtils.colorize(pC(p, value));
        }
        return pC(p, value);
    }

    public static String getMsg(Player p, String path) {
        return p(p, HLootchest.messagesConfig.getString("Messages." + path).replace("{prefix}", HLootchest.messagesConfig.getString("Messages.prefix")));
    }

    public static void sendHelp(Player p, String target) {
        List<String> helps = HLootchest.messagesConfig.getList("Messages." + target + "-help");
        if (helps == null) {
            return;
        }
        for (String s : helps) {
            p.sendMessage(p(p, s));
        }
    }

    public static String c(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
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
        return String.format("%s, %.2f, %.2f, %.2f, %.2f, %.2f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch());
    }

    public static boolean isXYZFormat(String str) {
        return str.matches("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$");
    }

    public static String getPrevPath(String path) {
        String[] elements = path.split("\\.");
        return String.join(".", Arrays.copyOf(elements, elements.length - 1));
    }

    public static <T> Set<T> getRandomElements(Set<T> set, List<Integer> chances, int n) {
        if (set.size() != chances.size()) {
            throw new IllegalArgumentException("The size of the set and the chances list must be the same.");
        }

        int totalChance = chances.get(chances.size()-1);

        List<T> list = new ArrayList<>(set);
        Set<T> randomElements = new HashSet<>();
        Random random = new Random();

        if (n > set.size()) {
            throw new IllegalArgumentException("Cannot select more unique elements than are in the set.");
        }

        while (randomElements.size() < n) {
            int randValue = random.nextInt(totalChance);

            int i = closestGreater(chances, randValue);
            randomElements.add(list.get(i));
        }

        return randomElements;

    }

    public static int closestGreater(List<Integer> A, int x) {
        int lo = 0, hi = A.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (A.get(mid) <= x) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    public static boolean checkConditions(Player p, TConfigManager config, String path) {
        if (!config.getYml().contains(path)) {
            return true;
        }
        Set<String> conditions = config.getYml().getConfigurationSection(path).getKeys(false);
        for (String element : conditions) {
            String absolutePath = path + "." + element;
            String type = config.getString(absolutePath + ".type");
            if (type.equals("has-permission")) {
                if (!p.hasPermission(config.getString(absolutePath + ".value"))) {
                    return false;
                }
            }
            String val1 = config.getString(absolutePath + ".input");
            String val2 = config.getString(absolutePath + ".output");
            switch (type) {
                case "string-equals":
                    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                        info("Missing PlaceholderAPI, this comparision will not work");

                        return false;
                    }
                    if (!val1.equals(PlaceholderAPI.setPlaceholders(p, val2))) {
                        return false;
                    }
                    break;
                case ">=":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) >= Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
                case "<=":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) <= Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
                case ">":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) > Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
                case "<":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) < Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
                case "==":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) == Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
                case "!=":
                    if (!isNumeric(val1) || !isNumeric(val2)) {
                        return false;
                    }
                    if (!(Double.parseDouble(val1) != Double.parseDouble(val2))) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        if (str == null) return true;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void clean(Chunk chunk, String name) {
        boolean isLoaded = chunk.isLoaded();
        if (!isLoaded) {
            chunk.load();
        }
        for (Entity entity : chunk.getEntities()) {
            if (entity.getCustomName() != null && entity.getCustomName().contains(name) && !(entity instanceof Player)) {
                entity.remove();
            }
        }

        if (!isLoaded) {
            chunk.unload();
        }
    }
}


package org.twightlight.hlootchest.supports.protocol.v1_19_R3;

import org.twightlight.libs.xseries.XMaterial;
import org.twightlight.libs.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;
import org.twightlight.hlootchest.api.interfaces.lootchest.TButton;
import org.twightlight.hlootchest.api.version_supports.NMSHandler;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.boxes.BoxManager;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.buttons.Button;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.listeners.ClickEvent;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.supports.PacketEventsSupport;
import org.twightlight.hlootchest.supports.protocol.v1_19_R3.utilities.NMSUtil;
import org.twightlight.hlootchest.utils.ColorUtils;
import org.twightlight.hlootchest.utils.Utility;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    public Main(Plugin pl, String name, HLootchest api1) {
        super(pl, name);
        handler = this;
        colorUtils = new ColorUtils();
        api = api1;
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

    public void registerButtonClick(Player player) {
    }

    public TButton spawnButton(Location location, ButtonType type, Player player, String path, TYamlWrapper config) {
        return new Button(location, type, player, path, config, false);
    }

    public TButton spawnPreviewButton(Location location, ButtonType type, Player player, String path, TYamlWrapper config) {
        return new Button(location, type, player, path, config, true);
    }

    public TBox spawnBox(Location location, String boxid, Player player, ItemStack icon, TYamlWrapper config) {
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
        ItemStack i = handler.createItemStack(XMaterial.matchXMaterial(material.name()).get().name(), 1, Short.valueOf("0"));
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
        itemMeta.setCustomModelData(data);
        i.setItemMeta(itemMeta);

        if (material == XMaterial.PLAYER_HEAD.get() && headUrl != null && !headUrl.trim().isEmpty()) {
            try {
                SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                if (api.getHooksLoader().getHeadDatabaseHook().hasHeadDatabase()) {
                    if (headUrl.contains(":")) {
                        String[] value = headUrl.split(":", 2);
                        if (value[0].equals("hdb")) {
                            headUrl = api.getHooksLoader().getHeadDatabaseHook().getHeadDatabaseService().getBase64(value[1]);
                        }
                    }
                }
                if (Utility.isValidBase64(headUrl)) {
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

    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.v1_19_R3;
    }
}

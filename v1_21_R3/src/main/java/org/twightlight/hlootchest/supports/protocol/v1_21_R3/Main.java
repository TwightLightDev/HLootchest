package org.twightlight.hlootchest.supports.protocol.v1_21_R3;

import org.twightlight.libs.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.twightlight.hlootchest.api.HLootchest;
import org.twightlight.hlootchest.api.enums.ProtocolVersion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main extends org.twightlight.hlootchest.supports.protocol.v1_19_R3.Main {

    public Main(Plugin pl, String name, HLootchest api) {
        super(pl, name, api);
    }

    @Override
    public ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = handler.createItemStack(XMaterial.matchXMaterial(material.name()).get().name(), 1, (short)data);
        ItemMeta itemMeta = i.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (!lore.isEmpty()) {
            lore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            itemMeta.setLore(lore);

        }
        if (enchanted)
            itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemMeta.setCustomModelData(data);
        i.setItemMeta(itemMeta);
        if (material.equals(XMaterial.PLAYER_HEAD.get()) && headUrl != null) {
            SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

            PlayerTextures textures = profile.getTextures();
            if (api.getHooksLoader().getHeadDatabaseHook().hasHeadDatabase()) {
                if (headUrl.contains(":")) {
                    String[] value = headUrl.split(":", 2);
                    if (value[0].equals("hdb")) {
                        headUrl = api.getHooksLoader().getHeadDatabaseHook().getHeadDatabaseService().getBase64(value[1]);
                    }
                }
            }
            URL urlObject;
            try {
                urlObject = getUrlFromBase64(headUrl);
                if (urlObject != null) {
                    textures.setSkin(urlObject);
                    profile.setTextures(textures);
                    skullMeta.setOwnerProfile(profile);
                    i.setItemMeta(skullMeta);
                }
            } catch (MalformedURLException exception) {
                throw new RuntimeException("Invalid URL", exception);
            }
        }
        return i;
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        String decoded = new String(Base64.getDecoder().decode(base64));

        String prefix = "{\"textures\":{\"SKIN\":{\"url\":\"";
        String suffix = "\"}}}";

        if (decoded.length() < prefix.length() + suffix.length()) {
            return null;
        }

        return new URL(decoded.substring(prefix.length(), decoded.length() - suffix.length()));
    }

    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.v1_21_R3;
    }

}

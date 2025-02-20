package org.twightlight.hlootchest.supports.v1_20_R3;

import com.cryptomorin.xseries.XMaterial;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.twightlight.hlootchest.api.HLootchest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main extends org.twightlight.hlootchest.supports.v1_19_R3.Main {

    public Main(Plugin pl, String name, HLootchest api) {
        super(pl, name, api);
    }

    @Override
    public ItemStack createItem(Material material, String headUrl, int data, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = handler.createItemStack(XMaterial.matchXMaterial(material).name(), 1, (short)data);
        ItemMeta itemMeta = i.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
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
        if (material.equals(XMaterial.PLAYER_HEAD.parseMaterial()) && headUrl != null) {
            SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

            PlayerTextures textures = profile.getTextures();

            URL urlObject;
            try {
                urlObject = getUrlFromBase64(headUrl);
            } catch (MalformedURLException exception) {
                throw new RuntimeException("Invalid URL", exception);
            }
            textures.setSkin(urlObject);
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);

            i.setItemMeta(skullMeta);
        }
        return i;
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    @Override
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
            ((CraftPlayer)p).getHandle().c.a(packet);
    }
}

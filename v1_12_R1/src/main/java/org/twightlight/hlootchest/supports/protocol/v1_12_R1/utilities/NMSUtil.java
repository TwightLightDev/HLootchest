package org.twightlight.hlootchest.supports.protocol.v1_12_R1.utilities;

import org.twightlight.libs.xseries.XPotion;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.api.enums.ItemSlot;
import org.twightlight.hlootchest.api.interfaces.internal.NMSService;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.api.interfaces.lootchest.TIcon;
import org.twightlight.hlootchest.supports.protocol.v1_12_R1.Main;

import java.util.Set;

public class NMSUtil implements NMSService {
    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isNameEnable) {
        return createArmorStand(p, location, name, false, isNameEnable);
    }

    public ArmorStand createArmorStand(Player p, Location location, String name, boolean isSmall, boolean isNameEnable) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, location.getX(), location.getY(), location.getZ());

        armorStand.setCustomNameVisible(isNameEnable);
        armorStand.setCustomName(Main.api.getLanguageUtil().p(p, ChatColor.translateAlternateColorCodes('&', name)));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setSmall(isSmall);

        armorStand.yaw = location.getYaw();
        armorStand.pitch = location.getPitch();

        return (ArmorStand) armorStand.getBukkitEntity();
    }

    public void sendSpawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving((EntityLiving) nmsEntity);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void sendDespawnPacket(Player player, Entity entityLiving) {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        if (nmsEntity instanceof EntityLiving) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nmsEntity.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void equipIcon(Player p, ArmorStand entityLiving, TIcon icon) {
        equipIcon(p, entityLiving, icon.getItemStack(), icon.getItemSlot());
    }

    public void equipIcon(Player p, ArmorStand entityLiving, ItemStack bukkiticon, ItemSlot slot) {
        equipIcon(p, ((CraftArmorStand) entityLiving).getHandle(), bukkiticon, slot);
    }

    public void equipIcon(Player p, EntityArmorStand nmsEntity, ItemStack bukkiticon, ItemSlot slot) {
        if (bukkiticon != null) {
            net.minecraft.server.v1_12_R1.ItemStack icon = CraftItemStack.asNMSCopy(bukkiticon);
            EnumItemSlot slotint = EnumItemSlot.MAINHAND;
            switch (slot) {
                case HEAD:
                    slotint = EnumItemSlot.HEAD;
                    break;
                case CHESTPLATE:
                    slotint = EnumItemSlot.CHEST;
                    break;
                case LEGGINGS:
                    slotint = EnumItemSlot.LEGS;
                    break;
                case BOOTS:
                    slotint = EnumItemSlot.FEET;
                    break;
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    slotint = EnumItemSlot.OFFHAND;
                    break;
            }
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(
                    nmsEntity.getId(),
                    slotint,
                    icon
            );
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void teleport(Player player, Entity entityLiving, Location location) {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) entityLiving).getHandle();
        nmsEntity.setLocation(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(nmsEntity);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void lockAngle(Player p, Location loc, long duration) {
        (new BukkitRunnable() {
            long startTime = System.currentTimeMillis();
            public void run() {
                if (System.currentTimeMillis() - this.startTime > duration * 50)
                    cancel();
                if (p.getLocation().getYaw() != loc.getYaw() || p.getLocation().getPitch() != loc.getPitch()) {
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }).runTaskTimer(Main.handler.plugin, 0L, 2L);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T summonVehicle(Location loc, Class<T> entityClass) {
        EntityType entityType = getEntityType(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException("Unsupported entity class: " + entityClass.getName());
        }

        Entity vehicle = loc.getWorld().spawnEntity(loc.clone().add(0, -0.3, 0), entityType);

        if (vehicle instanceof LivingEntity) {
            ((LivingEntity) vehicle).addPotionEffect(new PotionEffect(XPotion.INVISIBILITY.getPotionEffectType(), Integer.MAX_VALUE, 1, false, false));
        }

        vehicle.setCustomName("LootchestVehicle");
        vehicle.setCustomNameVisible(false);

        if (vehicle instanceof CraftEntity) {
            net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) vehicle).getHandle();
            NBTTagCompound tag = new NBTTagCompound();

            nmsEntity.c(tag);

            tag.setInt("NoAI", 1);
            tag.setInt("NoGravity", 1);

            nmsEntity.f(tag);
        }
        return (T) vehicle;
    }

    private EntityType getEntityType(Class<? extends Entity> entityClass) {
        for (EntityType type : EntityType.values()) {
            if (type.getEntityClass() != null && type.getEntityClass().equals(entityClass)) {
                return type;
            }
        }
        return null;
    }

    public void drawCircle(Player player, ArmorStand armorStand, Location center, double radius, double rotX, double rotY, double rotZ, int points) {
        Animations.DrawCircle(player, ((CraftArmorStand) armorStand).getHandle(), center, radius, points, rotX, rotY, rotZ);
    }

    public void moveBackward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveBackward(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }

    public void moveForward(Player player, ArmorStand armorStand, float val) {
        Animations.MoveForward(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }

    public void moveUp(Player player, ArmorStand armorStand, double val) {
        Animations.MoveUp(player, ((CraftArmorStand) armorStand).getHandle(), (float) val);
    }

    public void spin(Player player, ArmorStand armorStand, float val) {
        Animations.Spinning(player, ((CraftArmorStand) armorStand).getHandle(), val);
    }

    public void setNmsBlock(Player p, Location loc, Material to, BlockFace facing) {
        if (loc == null || to == null) return;

        BlockPosition blockPos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) loc.getWorld()).getHandle();
        net.minecraft.server.v1_12_R1.Block newBlock = net.minecraft.server.v1_12_R1.Block.getById(to.getId());

        if (newBlock == null) return;

        IBlockData blockData = newBlock.getBlockData();

        if (newBlock instanceof BlockFurnace || newBlock instanceof BlockPumpkin || newBlock instanceof BlockDispenser) {
            blockData = (newBlock).fromLegacyData(getBlockFaceData(facing));
        }

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(world, blockPos);
        packet.block = blockData;

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    private int getBlockFaceData(BlockFace face) {
        switch (face) {
            case SOUTH: return 3;
            case WEST: return 4;
            case EAST: return 5;
            default: return 2;
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
    public void rotate(ArmorStand armorStandbukkit, TYamlWrapper config, String path) {
        EntityArmorStand armorStand = ((CraftArmorStand) armorStandbukkit).getHandle();
        rotate(armorStand, config, path);
    }

    public void rotate(EntityArmorStand armorStand, TYamlWrapper config, String path) {
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
}

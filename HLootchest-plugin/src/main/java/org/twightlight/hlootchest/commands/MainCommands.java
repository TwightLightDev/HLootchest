package org.twightlight.hlootchest.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.twightlight.hlootchest.HLootchest;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.objects.TBox;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.config.ConfigManager;
import org.twightlight.hlootchest.supports.v1_8_R3.v1_8_R3;
import org.twightlight.hlootchest.utils.Utility;

import java.io.File;
import java.util.*;


public class MainCommands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1) {
                if (HLootchest.getNms().getBoxFromPlayer(p) == null) {
                    String identifier = "regular";
                    TConfigManager conf = HLootchest.getAPI().getConfigUtil().getBoxesConfig();
                    ItemStack icon = Utility.createItem(Material.valueOf(conf.getString(identifier + ".icon.material")), conf.getString(identifier + ".icon.head_value"), conf.getInt(identifier + ".icon.data"), "", new ArrayList<>(), false);
                    TConfigManager templateconfig = HLootchest.getAPI().getConfigUtil().getTemplateConfig();
                    Location location = HLootchest.getNms().stringToLocation(templateconfig.getString(identifier + ".settings.location"));

                    HLootchest.getNms().spawnBox(location, identifier, p, icon, templateconfig, p.getLocation());

                    if (templateconfig.getYml().getConfigurationSection(identifier + ".buttons") != null) {
                        Set<String> buttons = templateconfig.getYml().getConfigurationSection(identifier + ".buttons").getKeys(false);

                        PriorityQueue<ButtonTask> taskQueue = new PriorityQueue<>(Comparator.comparingInt(ButtonTask::getDelay));

                        for (String button : buttons) {
                            String path = identifier + ".buttons." + button;
                            int delay = templateconfig.getYml().contains(path + ".delay") ? templateconfig.getInt(path + ".delay") : 0;

                            taskQueue.add(new ButtonTask(path, delay));
                        }

                        if (taskQueue.isEmpty()) {
                            return true;
                        }

                        new BukkitRunnable() {
                            int currentTick = 0;
                            @Override
                            public void run() {
                                // Process all tasks that should be run now
                                while (!taskQueue.isEmpty() && taskQueue.peek().getDelay() <= currentTick) {
                                    ButtonTask task = taskQueue.poll(); // Get the task with the smallest delay
                                    String path = task.getPath();

                                    // Retrieve button icon and location
                                    String iconMaterial = templateconfig.getString(path + ".icon.material");
                                    String iconHeadValue = templateconfig.getString(path + ".icon.head_value");
                                    int iconData = templateconfig.getInt(path + ".icon.data");
                                    String locationString = templateconfig.getString(path + ".location");

                                    // Create the icon item and spawn button
                                    ItemStack buttonIcon = Utility.createItem(Material.valueOf(iconMaterial), iconHeadValue, iconData, "", new ArrayList<>(), false);
                                    Location location = HLootchest.getNms().stringToLocation(locationString);


                                    HLootchest.getNms().spawnButton(location, ButtonType.FUNCTIONAL, p, buttonIcon, path, templateconfig);;
                                }

                                currentTick++;
                            }
                        }.runTaskTimer(HLootchest.getInstance(), 0L, 1L);
                    }
                }
            } else {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        HLootchest.getAPI().getConfigUtil().getTemplateConfig().reload();
                        HLootchest.getAPI().getConfigUtil().getBoxesConfig().reload();
                        p.sendMessage(Utility.c("&aHlootchest has been reloaded!"));
                        return true;
                    case "template":
                        if (args.length > 2) {
                            String name = args[2].toLowerCase();
                            File file = new File(HLootchest.getFilePath()+ "/templates", name + ".yml");
                            switch (args[1].toLowerCase()) {
                                case "delete":
                                    boolean isDeleted = file.delete();
                                    if (!isDeleted) {
                                        p.sendMessage(ChatColor.RED + "Template not found!");
                                    }
                                case "select":
                                    if (file.exists()) {
                                        HLootchest.getAPI().getConfigUtil().getMainConfig().set("template", name);
                                        HLootchest.templateConfig = new ConfigManager(HLootchest.getInstance(), HLootchest.getAPI().getConfigUtil().getMainConfig().getString("template"), HLootchest.getFilePath() + "/templates");
                                        p.sendMessage(Utility.c("&aSucessfully set template to ") + ChatColor.WHITE + name + Utility.c("&a!"));
                                    } else {
                                        p.sendMessage(Utility.c("&cTemplate ") + ChatColor.WHITE + name + Utility.c(" &cnot found!"));
                                    }
                            }
                        } else {
                            p.sendMessage(Utility.c("&cPlease enter template name"));
                        }
                    case "leave":
                        TBox box = HLootchest.getNms().getBoxFromPlayer(p);
                        if (HLootchest.getNms().getBoxFromPlayer(p) != null) {
                            box.removeVehicle(p);
                            box.getOwner().teleport(box.getPlayerInitialLoc());
                            box.remove();
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                if (!online.equals(p)) {
                                    online.showPlayer(p);
                                }
                            }
                            HLootchest.getNms().removeButtonsFromPlayer(p, ButtonType.FUNCTIONAL);
                            HLootchest.getNms().removeButtonsFromPlayer(p, ButtonType.REWARD);
                        }

                }
            }
        }
        return true;
    }

    private static class ButtonTask {
        private final String path;
        private final int delay;

        public ButtonTask(String path, int delay) {
            this.path = path;
            this.delay = delay;
        }

        public String getPath() {
            return path;
        }

        public int getDelay() {
            return delay;
        }
    }
}



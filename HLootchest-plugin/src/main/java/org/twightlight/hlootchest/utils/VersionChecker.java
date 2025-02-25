package org.twightlight.hlootchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class VersionChecker {
    private final JavaPlugin plugin;
    private final String resourceId;

    public VersionChecker(JavaPlugin plugin, String resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion = getLatestVersion();
                String currentVersion = plugin.getDescription().getVersion();

                if (latestVersion == null) {
                    plugin.getLogger().warning("Could not fetch latest version.");
                    return;
                }

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    notifyUpdateAvailable(currentVersion, latestVersion);
                } else {
                    plugin.getLogger().info("HLootChest is up to date!");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws Exception {
        URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }

    private void notifyUpdateAvailable(String currentVersion, String latestVersion) {
        String updateMessage = String.format(
                "§6[Updater] §eA new version is available! Current: §c%s §e→ Latest: §a%s\n" +
                        "§6[Updater] §eDownload it here: §bhttps://www.spigotmc.org/resources/%s/",
                currentVersion, latestVersion, resourceId
        );

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(updateMessage);

        Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .forEach(player -> player.sendMessage(updateMessage));
    }
}

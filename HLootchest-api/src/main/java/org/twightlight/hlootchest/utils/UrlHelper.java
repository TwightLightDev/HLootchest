package org.twightlight.hlootchest.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class UrlHelper {

    private static final String PREFIX = "\u001B[36m[HLootchest]\u001B[0m ";
    private static final int BAR_WIDTH = 40;
    private static final long UPDATE_INTERVAL_MS = 3000;

    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GRAY   = "\u001B[90m";

    public void download(String fileURL, File target) {

        long lastUpdate = 0;

        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            long totalSize = connection.getContentLengthLong();

            long startTime = System.currentTimeMillis();
            long downloaded = 0;

            System.out.println(PREFIX + CYAN + "Starting download: "
                    + RESET + target.getName());

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(target);
                 BufferedOutputStream out = new BufferedOutputStream(fos, 8192)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    downloaded += bytesRead;
                    out.write(buffer, 0, bytesRead);

                    long now = System.currentTimeMillis();

                    if (now - lastUpdate >= UPDATE_INTERVAL_MS || downloaded >= totalSize) {

                        long elapsed = now - startTime;
                        if (elapsed <= 0) elapsed = 1;

                        double speed = downloaded / (elapsed / 1000.0);
                        long eta = speed > 0
                                ? (long) ((totalSize - downloaded) / speed)
                                : 0;

                        printProgressBar(
                                target.getName(),
                                downloaded,
                                totalSize,
                                speed,
                                eta
                        );

                        lastUpdate = now;
                    }
                }
            }

            System.out.println(PREFIX + GREEN + "Download complete: "
                    + RESET + target.getName());
            System.out.println();

        } catch (Exception e) {
            System.err.println(PREFIX + YELLOW + "Failed to download: "
                    + RESET + target.getName());
            e.printStackTrace();
        }
    }

    private void printProgressBar(String fileName,
                                  long downloaded,
                                  long total,
                                  double speed,
                                  long etaSeconds) {

        int percent = total > 0 ? (int) ((downloaded * 100) / total) : 0;
        int filled = (percent * BAR_WIDTH) / 100;

        StringBuilder bar = new StringBuilder();
        bar.append("[");

        bar.append(GREEN);
        for (int i = 0; i < filled; i++) bar.append("█");

        bar.append(GRAY);
        for (int i = filled; i < BAR_WIDTH; i++) bar.append("░");

        bar.append(RESET).append("] ");

        System.out.println(
                PREFIX +
                        CYAN + "Downloading " + RESET + fileName
        );

        System.out.println(
                bar +
                        YELLOW + percent + "%" + RESET + "  " +
                        formatSize(downloaded) + "/" + formatSize(total) +
                        GRAY + " | " + RESET +
                        formatSpeed(speed) +
                        GRAY + " | ETA " + RESET +
                        formatTime(etaSeconds)
        );

        System.out.println();
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024)
            return new DecimalFormat("#.##")
                    .format(bytes / 1024.0 / 1024.0) + " MB";
        if (bytes >= 1024)
            return new DecimalFormat("#.##")
                    .format(bytes / 1024.0) + " KB";
        return bytes + " B";
    }

    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond >= 1024 * 1024)
            return new DecimalFormat("#.##")
                    .format(bytesPerSecond / 1024.0 / 1024.0) + " MB/s";
        if (bytesPerSecond >= 1024)
            return new DecimalFormat("#.##")
                    .format(bytesPerSecond / 1024.0) + " KB/s";
        return new DecimalFormat("#.##")
                .format(bytesPerSecond) + " B/s";
    }

    private String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
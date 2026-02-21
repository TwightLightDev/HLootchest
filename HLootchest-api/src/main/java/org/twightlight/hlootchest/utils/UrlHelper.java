package org.twightlight.hlootchest.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class UrlHelper {

    private static final String PREFIX = "[HLootchest] ";
    private static final int BAR_WIDTH = 40;
    private static final char[] SPINNER = {'|', '/', '-', '\\'};

    public void download(String fileURL, File target) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            long totalSize = connection.getContentLengthLong();

            long startTime = System.currentTimeMillis();
            long downloaded = 0;
            int spinnerIndex = 0;

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(target);
                 BufferedOutputStream out = new BufferedOutputStream(fos, 8192)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    downloaded += bytesRead;
                    out.write(buffer, 0, bytesRead);

                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed == 0) elapsed = 1;

                    double speed = downloaded / (elapsed / 1000.0);
                    long eta = speed > 0 ? (long) ((totalSize - downloaded) / speed) : 0;

                    printProgressBar(
                            target.getName(),
                            downloaded,
                            totalSize,
                            speed,
                            eta,
                            SPINNER[spinnerIndex]
                    );

                    spinnerIndex = (spinnerIndex + 1) % SPINNER.length;

                    Thread.sleep(50);
                }
            }

            printProgressBar(target.getName(), totalSize, totalSize, 0, 0, '✓');
            System.out.println();

        } catch (Exception e) {
            System.out.println();
            System.err.println(PREFIX + "Failed to download: " + target.getName());
            e.printStackTrace();
        }
    }

    private void printProgressBar(String fileName,
                                  long downloaded,
                                  long total,
                                  double speed,
                                  long etaSeconds,
                                  char spinner) {

        int percent = total > 0 ? (int) ((downloaded * 100) / total) : 0;
        int filled = (percent * BAR_WIDTH) / 100;

        StringBuilder bar = new StringBuilder();
        bar.append("\r").append(PREFIX)
                .append(spinner).append(" ")
                .append(fileName).append(" [");

        for (int i = 0; i < filled; i++) bar.append("=");
        for (int i = filled; i < BAR_WIDTH; i++) bar.append(" ");

        bar.append("] ")
                .append(String.format("%3d", percent)).append("% ")
                .append(formatSize(downloaded)).append("/")
                .append(formatSize(total)).append(" ")
                .append("| ")
                .append(formatSpeed(speed)).append(" ")
                .append("| ETA ")
                .append(formatTime(etaSeconds));

        System.out.print(bar);
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024)
            return new DecimalFormat("#.##").format(bytes / 1024.0 / 1024.0) + " MB";
        if (bytes >= 1024)
            return new DecimalFormat("#.##").format(bytes / 1024.0) + " KB";
        return bytes + " B";
    }

    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond >= 1024 * 1024)
            return new DecimalFormat("#.##").format(bytesPerSecond / 1024.0 / 1024.0) + " MB/s";
        if (bytesPerSecond >= 1024)
            return new DecimalFormat("#.##").format(bytesPerSecond / 1024.0) + " KB/s";
        return new DecimalFormat("#.##").format(bytesPerSecond) + " B/s";
    }

    private String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
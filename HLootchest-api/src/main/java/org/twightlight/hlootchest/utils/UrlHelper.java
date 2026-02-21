package org.twightlight.hlootchest.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlHelper {

    public void download(String fileURL, File target) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            long completeFileSize = httpConnection.getContentLengthLong();

            try (BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(target);
                 BufferedOutputStream bout = new BufferedOutputStream(fos, 1024)) {

                byte[] data = new byte[1024];
                long downloadedFileSize = 0;
                int bytesRead;

                while ((bytesRead = in.read(data, 0, 1024)) >= 0) {
                    downloadedFileSize += bytesRead;
                    bout.write(data, 0, bytesRead);

                    int percent = (int) ((downloadedFileSize * 100) / completeFileSize);
                    System.out.print("\rDownloading: " + percent + "%");
                }
            }

            System.out.println("\nDownload complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

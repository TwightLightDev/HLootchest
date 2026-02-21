package org.twightlight.hlootchest.dependency;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MavenDownloader {

    private final Repository repository;

    public MavenDownloader(Repository repository) {
        this.repository = repository;
    }

    public void download(String remotePath, Path target) throws Exception {

        Files.createDirectories(target.getParent());

        URL url = new URL(repository.getUrl() + remotePath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "HLootChest-DependencyLoader");

        InputStream in = connection.getInputStream();
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }
}

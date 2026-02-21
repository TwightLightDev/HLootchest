package org.twightlight.hlootchest.dependency;

import org.twightlight.hlootchest.utils.Utility;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DownloadManager {

    private final Path folder;
    private final Repository repository;
    private final ClassLoader parent;

    private URLClassLoader loader;

    public DownloadManager(Path folder,
                           Repository repository,
                           ClassLoader parent) {
        this.folder = folder;
        this.repository = repository;
        this.parent = parent;
    }

    public void load(Dependency... dependencies) throws Exception {

        Files.createDirectories(folder);

        Set<Dependency> all = new HashSet<Dependency>();
        all.addAll(Arrays.asList(dependencies));

        List<URL> urls = new ArrayList<URL>();
        MavenDownloader downloader = new MavenDownloader(repository);

        for (Dependency dep : all) {

            Path file = folder.resolve(
                    dep.getArtifact() + "-" + dep.getVersion() + ".jar"
            );

            if (!Files.exists(file)) {
                Utility.info("[HLootChest] Downloading " + dep.getArtifact());
                downloader.download(dep.getPath(), file);
            }

            urls.add(file.toUri().toURL());
        }

        loader = new URLClassLoader(
                urls.toArray(new URL[urls.size()]),
                parent
        );

    }

    public ClassLoader getLoader() {
        return loader;
    }
}
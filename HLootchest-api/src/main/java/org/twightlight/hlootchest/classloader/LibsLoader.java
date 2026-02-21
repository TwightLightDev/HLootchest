package org.twightlight.hlootchest.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LibsLoader {

    private final Path libsDir;

    public LibsLoader(Path libsDir, ClassLoader pluginClassLoader) throws IOException {
        this.libsDir = libsDir;
        injectLibs(pluginClassLoader);
    }

    private void injectLibs(ClassLoader pluginClassLoader) throws IOException {
        if (!Files.isDirectory(libsDir)) {
            throw new IOException("Invalid libs directory: " + libsDir);
        }

        try (Stream<Path> paths = Files.list(libsDir)) {
            paths.filter(p -> p.toString().endsWith(".jar"))
                    .forEach(jar -> addToClasspath(jar.toFile(), pluginClassLoader));
        }
    }

    private void addToClasspath(File file, ClassLoader classLoader) {
        try {
            if (classLoader instanceof URLClassLoader) {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(classLoader, file.toURI().toURL());
            } else {
                Method addPath = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                addPath.setAccessible(true);
                addPath.invoke(classLoader, file.toURI().toURL());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject library: " + file.getName(), e);
        }
    }
}

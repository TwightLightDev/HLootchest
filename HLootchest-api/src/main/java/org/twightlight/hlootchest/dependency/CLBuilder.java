package org.twightlight.hlootchest.dependency;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CLBuilder {

    public static Classloader build(File libsFolder,
                                    ClassLoader parent) throws Exception {

        File[] jars = libsFolder.listFiles(
                file -> file.getName().endsWith(".jar"));

        List<URL> urls = new ArrayList<URL>();

        if (jars != null) {
            for (File jar : jars) {
                urls.add(jar.toURI().toURL());
            }
        }

        return new Classloader(
                urls.toArray(new URL[urls.size()]),
                parent
        );
    }
}
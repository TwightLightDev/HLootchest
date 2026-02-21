package org.twightlight.hlootchest.dependency;

import java.net.URL;
import java.net.URLClassLoader;

public class Classloader extends URLClassLoader {

    public Classloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (name.startsWith("java.") ||
                name.startsWith("javax.") ||
                name.startsWith("org.bukkit.") ||
                name.startsWith("net.minecraft.")) {

            return super.loadClass(name, resolve);
        }

        try {
            Class<?> clazz = findClass(name);
            if (resolve) resolveClass(clazz);
            return clazz;
        } catch (ClassNotFoundException ignored) {}

        return super.loadClass(name, resolve);
    }
}
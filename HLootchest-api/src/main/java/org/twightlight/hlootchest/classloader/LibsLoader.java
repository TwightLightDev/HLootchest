package org.twightlight.hlootchest.classloader;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;

public class LibsLoader extends ClassLoader {

    private final Map<String, byte[]> classBytes = new HashMap<>();

    public LibsLoader(Path libsDir) throws IOException {
        super(null);
        loadAllJars(libsDir);
    }

    private void loadAllJars(Path libsDir) throws IOException {
        if (!Files.isDirectory(libsDir)) {
            throw new IOException("Invalid libs directory: " + libsDir);
        }

        try (Stream<Path> paths = Files.list(libsDir)) {
            for (Path jar : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".jar"))::iterator) {
                loadJar(jar);
            }
        }
    }

    private void loadJar(Path jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace('/', '.')
                            .substring(0, entry.getName().length() - 6);
                    try (InputStream in = jar.getInputStream(entry)) {
                        byte[] bytes = toByteArray(in);
                        classBytes.put(className, bytes);
                    }
                }
            }
        }
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classBytes.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }

        if (name.startsWith("java.") || name.startsWith("javax.")) {
            try {
                return getSystemClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Core Java class not found: " + name, e);
            }
        }

        throw new ClassNotFoundException(name);
    }

}

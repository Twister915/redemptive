package tech.rayline.core.library;

import lombok.Data;
import org.bukkit.plugin.java.JavaPlugin;
import tech.rayline.core.inject.Inject;
import tech.rayline.core.inject.Injectable;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

public final class LibraryHandler {
    public static void loadLibraries(RedemptivePlugin plugin) {
        Set<MavenLibrary> libraries = getLibraries(plugin.getClass());
        load(plugin, libraries.toArray(new MavenLibrary[libraries.size()]));
    }

    public static void load(RedemptivePlugin plugin, MavenLibrary... libraries) {
        Set<File> jars = new HashSet<>();
        for (MavenLibrary library : libraries) {
            try {
                ParsedLibrary parsedLibrary = parseLibrary(library);
                File location = createAndGetWriteLocation(parsedLibrary);
                if (!location.exists()) {
                    plugin.getLogger().info("Downloading " + getFileName(parsedLibrary) + " from " + library.repo().url());
                    try (InputStream inputStream = getUrl(library.repo(), parsedLibrary).openStream()) {
                        Files.copy(inputStream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    plugin.getLogger().info("Library " + library.value() + " is already downloaded!");
                }
                jars.add(location);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not load library " + library.value());
                e.printStackTrace();
            }
        }

        for (File jar : jars) {
            try {
                addFile(jar);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not load jar file " + jar.getName());
                continue;
            }

            plugin.getLogger().info("Loaded library " + jar.getName());
        }
    }

    private static ParsedLibrary parseLibrary(MavenLibrary library) {
        String[] split = library.value().split(":", 3);
        if (split.length != 3)
            throw new IllegalArgumentException("The library specified " + library.value() + " is bad!");
        return new ParsedLibrary(split[1], split[0], split[2]);
    }

    private static URL getUrl(MavenRepo repo, ParsedLibrary library) throws MalformedURLException {
        return new URL(repo.url() + "/" + getPath(library) + getFileName(library));
    }

    private static String getPath(ParsedLibrary library) {
        return library.group.replaceAll("\\.", "/") + "/" + library.artifact + "/" + library.version + "/";
    }

    private static String getFileName(ParsedLibrary library) {
        return library.artifact + "-" + library.version + ".jar";
    }

    private static File createAndGetWriteLocation(ParsedLibrary library) throws IOException {
        File rootDir = new File(".libs");
        if ((!rootDir.exists() || !rootDir.isDirectory()) && !rootDir.mkdir())
            throw new IOException("Could not create root directory .libs");

        File path = new File(rootDir, getPath(library));
        path.mkdirs();

        return new File(path, getFileName(library));
    }

    public static Set<MavenLibrary> getLibraries(Class<? extends RedemptivePlugin> plugin) {
        return getLibrariesRecurse(plugin, new HashSet<MavenLibrary>());
    }

    private static Set<MavenLibrary> getLibrariesRecurse(Class<?> targetClass, Set<MavenLibrary> libraries) {
        if (targetClass.isAnnotationPresent(IgnoreLibraries.class))
            return Collections.emptySet();

        MavenLibraries annotation = targetClass.getAnnotation(MavenLibraries.class);
        //actual annotations
        if (annotation != null)
            Collections.addAll(libraries, annotation.value());

        try {
            for (Field field : targetClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) continue;
                Injectable injectable = field.getType().getAnnotation(Injectable.class);
                if (injectable == null) continue;
                Collections.addAll(libraries, injectable.libraries());
            }
        } catch (Throwable t) {
            System.err.println("WARNING: Could not read fields for " + targetClass.getSimpleName());
        }

        targetClass = targetClass.getSuperclass();
        if (targetClass == Object.class || targetClass == JavaPlugin.class)
            return libraries;
        return getLibrariesRecurse(targetClass, libraries);
    }

    private static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    private static void addURL(URL u) throws IOException {
        //check if this is already loaded
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        }
        catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    @Data private static final class ParsedLibrary {
        private final String artifact, group, version;
    }
}

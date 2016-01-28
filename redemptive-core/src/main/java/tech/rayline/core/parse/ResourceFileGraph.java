package tech.rayline.core.parse;

import org.bukkit.plugin.java.JavaPlugin;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ResourceFileGraph {
    private final RedemptivePlugin plugin;
    private final Set<RegisteredResourceFile> registeredResources = new HashSet<>();
    private final Map<Class<? extends ResourceFileHook>, ResourceFileHook> fileHooks = new HashMap<>();

    public ResourceFileGraph(RedemptivePlugin plugin) {
        this.plugin = plugin;
    }

    public <T extends ResourceFileHook> T getResourceHook(Class<T> clazz) throws Exception {
        ResourceFileHook<?> resourceFileHook = fileHooks.get(clazz);
        if (resourceFileHook == null || !(resourceFileHook.getClass().equals(clazz))) {
            resourceFileHook = clazz.newInstance();
            fileHooks.put(clazz, resourceFileHook);
        }
        //noinspection unchecked
        return (T) resourceFileHook;
    }

    public void hookToObject(Object object) {
        hookToObject(object, object.getClass());
    }

    public void hookToObject(Object object, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            ResourceFile annotation = field.getAnnotation(ResourceFile.class);
            if (annotation == null) continue;
            try {
                ResourceFileHook resourceHook = getResourceHook(annotation.hook());
                File file = new File(plugin.getDataFolder(), annotation.filename());
                registeredResources.add(new RegisteredResourceFile(annotation, file, object, field, resourceHook));
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Could not register resource from " + type.getSimpleName() + "'s " + field.getName() + " field of type " + field.getType().getName());
            }
        }
        Class<?> superclass = type.getSuperclass();
        if (superclass == Object.class || superclass == JavaPlugin.class)
            return;

        hookToObject(object, superclass);
    }

    public void writeDefaults() {
        for (RegisteredResourceFile registeredResource : registeredResources)
            writeDefault(registeredResource);
    }

    private void writeDefault(RegisteredResourceFile resourceFile) {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) throw new IOException("Could not write default resource to data directory!");
            try (InputStream input = plugin.getResource(resourceFile.getAnnotation().filename())) {
                if (input == null) return;
                File file = resourceFile.getFile();
                if (file.exists())
                    return;

                if (!file.createNewFile())
                    throw new IOException("Could not create new file!");

                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = input.read(buffer)) != -1)
                        output.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("Could not write default for " + resourceFile.getAnnotation().filename());
        }
    }

    public void loadAll() {
        for (RegisteredResourceFile registeredResource : registeredResources)
            load(registeredResource);
    }

    private void load(RegisteredResourceFile registeredResource) {
        ResourceFile annotation = registeredResource.getAnnotation();
        try {
            Object object = registeredResource.getInstanceBound();
            Field field = registeredResource.getField();
            File file = registeredResource.getFile();
            if (!file.exists())
                file.createNewFile();

            ResourceFileHook<?> resourceHook = registeredResource.getHook();
            Object read;
            if (annotation.raw()) {
                read = resourceHook.readRaw(plugin, file);
            } else {
                read = resourceHook.read(plugin, file, field.getType());
            }
            field.setAccessible(true);
            if (read == null && field.get(object) != null)
                return;
            field.set(object, read);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not read/load resource " + annotation.filename());
        }
    }

    public void saveAll() {
        for (RegisteredResourceFile registeredResource : registeredResources)
            save(registeredResource);
    }

    public void save(Object instance, String fieldName) {
        for (RegisteredResourceFile registeredResource : registeredResources)
            if (registeredResource.getInstanceBound() == instance && registeredResource.getField().getName().equals(fieldName)) {
                save(registeredResource);
                return;
            }
    }

    public void saveAll(Object o) {
        for (RegisteredResourceFile registeredResource : registeredResources)
            if (registeredResource.getInstanceBound() == o)
                save(registeredResource);
    }

    private void save(RegisteredResourceFile registeredResource) {
        try {
            Field field = registeredResource.getField();
            field.setAccessible(true);
            if (field.isAnnotationPresent(ReadOnlyResource.class))
                return;

            Object value = field.get(registeredResource.getInstanceBound());
            ResourceFileHook<?> hook = registeredResource.getHook();

            File file = registeredResource.getFile();
            if (registeredResource.getAnnotation().raw())
                hook.writeRaw(plugin, value, file);
            else
                hook.write(plugin, value, file);
        }
        catch (Exception e) {
            e.printStackTrace();
            Field field = registeredResource.getField();
            plugin.getLogger().severe("Could not write resource from " + field.getType().getSimpleName() + " field named " + field.getName());
        }
        plugin.getLogger().info("Wrote resource " + registeredResource.getFile().getName() + " for " + registeredResource.getField().getName() +" on " + registeredResource.getInstanceBound().getClass().getSimpleName() + "!");
    }

    public void loadFor(Object object) {
        for (RegisteredResourceFile registeredResource : registeredResources)
            if (registeredResource.getInstanceBound() == object)
                load(registeredResource);
    }

    public void writeDefaultsFor(Object object) {
        for (RegisteredResourceFile registeredResource : registeredResources)
            if (registeredResource.getInstanceBound() == object)
                writeDefault(registeredResource);
    }
}

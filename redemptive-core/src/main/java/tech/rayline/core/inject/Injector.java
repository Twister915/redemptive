package tech.rayline.core.inject;

import org.bukkit.plugin.java.JavaPlugin;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Injector {
    public static Object[] injectTo(RedemptivePlugin plugin) throws Exception {
        return injectTo(plugin, plugin.getClass());
    }

    public static Object[] injectTo(RedemptivePlugin plugin, Class<?> clazz) throws Exception {
        List<Object> itemsCreated = new ArrayList<>();

        Field[] declaredFields = clazz.getDeclaredFields();
        FIELD_LOOP: for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(Inject.class))
                continue;
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
            if (!type.isAnnotationPresent(Injectable.class))
                continue;
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (!constructor.isAnnotationPresent(InjectionProvider.class))
                    continue;
                constructor.setAccessible(true);
                Object o = constructor.newInstance(plugin);
                itemsCreated.add(o);
                declaredField.set(plugin, o);
                continue FIELD_LOOP;
            }
            throw new IllegalStateException("Could not find a valid constructor for field " + declaredField.getName() + " with type " + declaredField.getType().getSimpleName());
        }

        Object[] objects = itemsCreated.toArray();

        Class<?> superclass = clazz.getSuperclass();
        if (shouldRecurse(superclass)) { //stop at JavaPlugin (or Object if we overshoot... somehow)
            Object[] recursiveObjects = injectTo(plugin, superclass);
            Object[] finalObjects = new Object[objects.length + recursiveObjects.length];
            System.arraycopy(objects, 0, finalObjects, 0, objects.length);
            System.arraycopy(recursiveObjects, 0, finalObjects, objects.length, recursiveObjects.length);
            return finalObjects;
        }

        return objects;
    }

    public static boolean shouldRecurse(Class<?> superclass) {
        return superclass != JavaPlugin.class && superclass != Object.class;
    }

    public static void handleDisable(Object[] injected) throws Exception {
        for (Object o : injected)
            handleDisable(o, o.getClass());
    }

    public static void handleDisable(Object object, Class<?> clazz) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(DisableHandler.class))
                continue;
            method.setAccessible(true);
            method.invoke(object);
        }

        Class<?> superclass = clazz.getSuperclass();
        if (shouldRecurse(superclass))
            handleDisable(object, superclass);
    }
}

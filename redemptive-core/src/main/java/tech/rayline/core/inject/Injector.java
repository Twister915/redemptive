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
        //iterates through all the fields that we might want to inject a dependency into
        FIELD_LOOP: for (Field declaredField : declaredFields) {
            //if we don't have the annotation, we don't care
            if (!declaredField.isAnnotationPresent(Inject.class))
                continue;
            //set it accessible
            declaredField.setAccessible(true);
            //get the type so we can check that it can be injected by...
            Class<?> type = declaredField.getType();
            //checking the annotation and...
            if (!type.isAnnotationPresent(Injectable.class))
                continue;
            //searching for the constructor
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                //which has the InjectionProvider annotation
                if (!constructor.isAnnotationPresent(InjectionProvider.class))
                    continue;
                //and once we find it, we set it accessible and create a new instance
                constructor.setAccessible(true);
                Object o = constructor.newInstance(plugin);
                //if successful (tons of exceptions being thrown up the stack above), we want to keep track of instance we just created
                itemsCreated.add(o);
                //and also set the field
                declaredField.set(plugin, o);
                continue FIELD_LOOP; //continue the outer loop to avoid the exception trap below
            }
            //if we never continued, we'll find ourselves here, meaning that we never found the constructor for this field's dependency
            //throw an exception- we've failed
            throw new IllegalStateException("Could not find a valid constructor for field " + declaredField.getName() + " with type " + declaredField.getType().getSimpleName());
        }

        //grab the objects that we've gotten this time around
        Object[] objects = itemsCreated.toArray();

        //do some recursion so we get all private fields
        Class<?> superclass = clazz.getSuperclass();
        if (shouldRecurse(superclass)) { //stop at JavaPlugin (or Object if we overshoot... somehow)
            Object[] recursiveObjects = injectTo(plugin, superclass);
            Object[] finalObjects = new Object[objects.length + recursiveObjects.length];
            //combine the arrays quickly
            System.arraycopy(objects, 0, finalObjects, 0, objects.length);
            System.arraycopy(recursiveObjects, 0, finalObjects, objects.length, recursiveObjects.length);
            return finalObjects; //return out the result
        }

        //if we didn't need to recurse we can just return stuff
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

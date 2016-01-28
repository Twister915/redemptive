package tech.rayline.core.parse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceFile {
    /**
     * @return The name of the file in the plugin data directory
     */
    String filename();

    /**
     * @return The resource hook for serialization
     */
    Class<? extends ResourceFileHook> hook() default SnakeYAMLResourceFileHook.class;

    /**
     * @return If we should serialize this as a bean or use it as a container for the raw type related to this resource file hook.
     */
    boolean raw() default false;
}

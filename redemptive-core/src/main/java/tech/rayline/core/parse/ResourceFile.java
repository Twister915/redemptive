package tech.rayline.core.parse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceFile {
    String filename();
    Class<? extends ResourceFileHook> hook() default SnakeYAMLResourceFileHook.class;
    boolean raw() default false;
}

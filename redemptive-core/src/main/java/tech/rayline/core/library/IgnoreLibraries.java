package tech.rayline.core.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * When any class in an inheritance tree has this annotation, all libraries will be ignored!
 */
public @interface IgnoreLibraries {}

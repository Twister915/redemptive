package tech.rayline.core.inject;

import tech.rayline.core.library.MavenLibrary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Used to mark classes which are to be injected so that we can tell IntelliJ that these classes are actually used indirectly
 */
public @interface Injectable {
    MavenLibrary[] libraries() default {};
}

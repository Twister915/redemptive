package tech.rayline.core.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.LOCAL_VARIABLE)
public @interface MavenLibrary {
    String value();
    MavenRepo repo() default @MavenRepo(url = "http://repo1.maven.org/maven2");
}

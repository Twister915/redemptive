package tech.rayline.core.persist;

import org.mongodb.morphia.mapping.DefaultCreator;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import tech.rayline.core.inject.Injectable;
import tech.rayline.core.inject.InjectionProvider;
import tech.rayline.core.library.MavenLibraries;
import tech.rayline.core.library.MavenLibrary;
import tech.rayline.core.plugin.RedemptivePlugin;

@Injectable(libraries = {@MavenLibrary("org.mongodb.morphia:morphia:1.0.1"), @MavenLibrary("org.mongodb:mongo-java-driver:3.0.2")})
public final class MorphiaHelper {
    private final RedemptivePlugin plugin;

    @InjectionProvider
    public MorphiaHelper(RedemptivePlugin plugin) {
        this.plugin = plugin;
    }

    public Mapper createNewMapper() {
        //this here is some magic to get Bukkit classes to work in the mapper
        MapperOptions mapperOptions = new MapperOptions();
        final ClassLoader ourClassLoader = plugin.getClass().getClassLoader();
        mapperOptions.setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return ourClassLoader;
            }
        });

        return new Mapper(mapperOptions);
    }
}

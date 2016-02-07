package tech.rayline.core.sql;

import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import tech.rayline.core.inject.DisableHandler;
import tech.rayline.core.inject.Injectable;
import tech.rayline.core.inject.InjectionProvider;
import tech.rayline.core.library.MavenLibrary;
import tech.rayline.core.parse.ReadOnlyResource;
import tech.rayline.core.parse.ResourceFile;
import tech.rayline.core.plugin.RedemptivePlugin;

import java.sql.Connection;
import java.sql.SQLException;

@Injectable(libraries = {@MavenLibrary("com.zaxxer:HikariCP:2.4.3"), @MavenLibrary("org.slf4j:slf4j-api:1.7.12")})
@Getter
public final class HikariCPBridge {
    @ResourceFile(filename = "database.yml") @ReadOnlyResource private DatabaseConfiguration databaseConfiguration;
    private final HikariPool pool;

    @InjectionProvider
    public HikariCPBridge(RedemptivePlugin plugin) {
        plugin.getResourceFileGraph().addObject(this);
        pool = new HikariPool(databaseConfiguration.getHikariConfig());
    }

    @DisableHandler
    private void onDisable() throws InterruptedException {
        pool.shutdown();
    }

    public boolean doesTableExist(String tableName) throws SQLException {
        try (Connection connection = pool.getConnection()) {
            return connection.getMetaData().getTables(null, null, tableName, null).first();
        }
    }
}

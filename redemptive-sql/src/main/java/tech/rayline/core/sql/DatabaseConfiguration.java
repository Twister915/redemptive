package tech.rayline.core.sql;

import com.zaxxer.hikari.HikariConfig;

public final class DatabaseConfiguration {
    public String host, database, username, password;
    public int port;

    public HikariConfig getHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        return hikariConfig;
    }
}

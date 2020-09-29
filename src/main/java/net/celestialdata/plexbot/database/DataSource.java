package net.celestialdata.plexbot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.celestialdata.plexbot.config.ConfigProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides a datasource for the Database connection pool
 *
 * @author Celestialdeath99
 */
public class DataSource {

    // Config and Datasource objects
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    /**
     * Initialize the database connection
     */
    public static void init() {
        config.setJdbcUrl("jdbc:mariadb://" + ConfigProvider.DATABASE_SETTINGS.ipAddress() + ":" + ConfigProvider.DATABASE_SETTINGS.port() + "/" + ConfigProvider.DATABASE_SETTINGS.dbName());
        config.setUsername(ConfigProvider.DATABASE_SETTINGS.username());
        config.setPassword(ConfigProvider.DATABASE_SETTINGS.password());
        config.setMaximumPoolSize(10);
        config.setMaxLifetime(300000);
        config.setConnectionTimeout(10000);
        config.setPoolName("Database Connection");
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);
    }

    /**
     * Close the connections to the database
     */
    public static void close() {
        dataSource.close();
    }

    /**
     * Provides a DB connection from the pool
     *
     * @return Return a connection
     * @throws SQLException If a connection error has occurred
     */
    static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}

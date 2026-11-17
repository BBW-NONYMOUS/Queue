package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** Creates the pool without JDBC access here; connections are borrowed by DAOs. */
public final class DatabasePool implements AutoCloseable {
    private final HikariDataSource dataSource;

    public DatabasePool(AppConfig config) {
        HikariConfig pool = new HikariConfig();
        pool.setPoolName("QueueClearance");
        pool.setJdbcUrl(config.jdbcUrl());
        pool.setUsername(config.databaseUsername());
        pool.setPassword(config.databasePassword());
        pool.setMaximumPoolSize(config.maximumPoolSize());
        pool.setMinimumIdle(config.minimumIdle());
        pool.setConnectionTimeout(config.connectionTimeoutMs());
        pool.setValidationTimeout(config.validationTimeoutMs());
        pool.setIdleTimeout(config.idleTimeoutMs());
        pool.setMaxLifetime(config.maxLifetimeMs());
        pool.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        pool.setInitializationFailTimeout(-1);
        pool.addDataSourceProperty("allowMultiQueries", "false");
        pool.addDataSourceProperty("sslMode", "VERIFY_IDENTITY");
        pool.addDataSourceProperty("connectionTimeZone", "+08:00");
        pool.addDataSourceProperty("forceConnectionTimeZoneToSession", "true");
        dataSource = new HikariDataSource(pool);
    }

    public HikariDataSource dataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}

package com.firma.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Менеджер подключений к БД.
 * Использует HikariCP для пула соединений.
 */
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private static final String PROPERTIES_FILE = "application.properties";
    private static volatile HikariDataSource dataSource;

    private ConnectionManager() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (ConnectionManager.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                    log.info("HikariCP connection pool инициализирован");
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP connection pool закрыт");
        }
    }

    private static HikariDataSource createDataSource() {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.setPoolName("FirmaPool");
        return new HikariDataSource(config);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = ConnectionManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) throw new RuntimeException("Файл " + PROPERTIES_FILE + " не найден в classpath!");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения " + PROPERTIES_FILE, e);
        }
        return props;
    }
}

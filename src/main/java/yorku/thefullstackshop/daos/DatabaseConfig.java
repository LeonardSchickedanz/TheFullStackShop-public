package yorku.thefullstackshop.daos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = loadProperties();
            HikariConfig config = new HikariConfig();

            String url = System.getenv("DB_URL");
            if (url == null || url.isEmpty()) {
                url = props.getProperty("db.url");
            }
            config.setJdbcUrl(url);

            String username = System.getenv("DB_USERNAME");
            if (username == null || username.isEmpty()) {
                username = props.getProperty("db.username");
            }
            config.setUsername(username);

            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isEmpty()) {
                password = props.getProperty("db.password");
            }
            config.setPassword(password);

            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));

            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setPoolName("TheFullStackShopHikariPool");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HikariCP DataSource", e);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.out.println("Info: Keine database.properties gefunden, verlasse mich auf Environment Variables.");
        }
        return props;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
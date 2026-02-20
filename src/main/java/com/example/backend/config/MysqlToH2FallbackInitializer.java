package com.example.backend.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * When the "mysql", "prod-mysql", or "prod" profile is active and datasource is MySQL,
 * tries to connect to MySQL. If the connection fails, overrides to an in-memory H2 database.
 */
public class MysqlToH2FallbackInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        if (!isMysqlProfileActive(env)) {
            return;
        }

        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username", "");
        String password = env.getProperty("spring.datasource.password", "");

        if (url == null || !url.startsWith("jdbc:mysql")) {
            return;
        }

        if (tryMySQLConnection(url, username, password)) {
            return;
        }

        // MySQL unreachable – override with H2 so auto-config picks it up
        ConfigurableEnvironment configurable = (ConfigurableEnvironment) env;
        Map<String, Object> h2Props = new LinkedHashMap<>();
        h2Props.put("spring.datasource.url", "jdbc:h2:mem:fallback;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        h2Props.put("spring.datasource.driver-class-name", "org.h2.Driver");
        h2Props.put("spring.datasource.username", "sa");
        h2Props.put("spring.datasource.password", "");
        h2Props.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        h2Props.put("spring.jpa.hibernate.ddl-auto", "update");
        h2Props.put("spring.h2.console.enabled", "true");
        configurable.getPropertySources().addFirst(new MapPropertySource("mysqlFallbackH2", h2Props));

        System.err.println("[MysqlToH2Fallback] MySQL connection failed. Using H2 in-memory database.");
    }

    private static boolean isMysqlProfileActive(Environment env) {
        String[] active = env.getActiveProfiles();
        return Arrays.stream(active).anyMatch(p ->
                "mysql".equals(p) || "prod-mysql".equals(p) || "prod".equals(p));
    }

    private static boolean tryMySQLConnection(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

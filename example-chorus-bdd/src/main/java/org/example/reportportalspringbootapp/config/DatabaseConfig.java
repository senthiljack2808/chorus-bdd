package org.example.reportportalspringbootapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Database configuration for external MySQL database
 */
@Configuration
public class DatabaseConfig {

    @Value("${external.db.url:jdbc:mysql://10.77.21.114:3306/testresults}")
    private String dbUrl;

    @Value("${external.db.username:testuser}")
    private String dbUsername;

    @Value("${external.db.password:testpass}")
    private String dbPassword;

    @Value("${external.db.driver:com.mysql.cj.jdbc.Driver}")
    private String dbDriver;

    /**
     * Creates a DataSource for external database connections
     *
     * @return Configured DataSource
     */
    @Bean(name = "externalDataSource")
    public DataSource externalDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    /**
     * Creates a JdbcTemplate for external database operations
     *
     * @return Configured JdbcTemplate
     */
    @Bean(name = "externalJdbcTemplate")
    public JdbcTemplate externalJdbcTemplate() {
        return new JdbcTemplate(externalDataSource());
    }
}

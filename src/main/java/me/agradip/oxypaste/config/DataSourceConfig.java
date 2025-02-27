package me.agradip.oxypaste.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = null;
        if (dataSourceUrl.startsWith("jdbc:sqlite")) {
            return sqliteDataSource(); // return the sqlite datasource if its sqlite because hikaricp doesn't support sqlite
        } else if (dataSourceUrl.startsWith("jdbc:mariadb")) {
            dataSource = (HikariDataSource) mysqlDataSource();
        } else if (dataSourceUrl.startsWith("jdbc:postgresql")) {
            dataSource = (HikariDataSource) postgresDataSource();
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + dataSourceUrl);
        }

        dataSource.setDriverClassName(driverClass);

        return dataSource;
    }

    private DataSource sqliteDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl(dataSourceUrl);
        return dataSource;
    }

    private DataSource mysqlDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setJdbcUrl(dataSourceUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private DataSource postgresDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(dataSourceUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}

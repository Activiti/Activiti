package org.activiti.spring.boot;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRegistration;
import javax.sql.DataSource;
import java.util.Map;

/**
 * unit tests for the REST API integration.
 *
 * @author Josh Long
 */
public class TestProcessEngineRestApiAutoConfiguration {

    @Configuration
    @EnableAutoConfiguration
    public static class SimpleDataSourceConfiguration {
        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        TaskExecutor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

        @Bean
        DataSource dataSource() {
            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setUsername("sa");
            basicDataSource.setUrl("jdbc:h2:mem:activiti");
            basicDataSource.setDefaultAutoCommit(false);
            basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
            basicDataSource.setPassword("");
            return basicDataSource;
        }

        @Bean
        PlatformTransactionManager dataSourceTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    private ConfigurableApplicationContext applicationContext;


    @After
    public void close() {
        this.applicationContext.close();
    }

    @Before
    public void setUp() {
        this.applicationContext = SpringApplication.run(SimpleDataSourceConfiguration.class);
    }

    @Test
    public void testRestApiIntegration() throws Throwable {
        for (ServletRegistration servletRegistration : this.applicationContext.getBeansOfType(ServletRegistration.class).values()) {
            System.out.println(servletRegistration);
        }
        String authenticationChallenge = "http://localhost:8080/api/process-definitions";

        RestTemplate restTemplate = this.applicationContext.getBean(RestTemplate.class );
        ResponseEntity<Map> x = restTemplate.getForEntity(authenticationChallenge, Map.class );
        org.junit.Assert.assertTrue(x.getStatusCode().equals(HttpStatus.FORBIDDEN));


    }
}

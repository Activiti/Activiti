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
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRegistration;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * unit tests for the REST API integration.
 *
 * @author Josh Long
 */
public class TestProcessEngineRestApiAutoConfiguration {

    @Configuration
    @EnableAutoConfiguration
    public static class TestConfiguration {

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
        this.applicationContext = SpringApplication.run(TestConfiguration.class);
    }

    @Test
    public void testRestApiIntegration() throws Throwable {
        for (ServletRegistration servletRegistration : this.applicationContext.getBeansOfType(ServletRegistration.class).values()) {
            System.out.println(servletRegistration);
        }
        String authenticationChallenge = "http://localhost:8080/api/process-definitions";

        RestTemplate restTemplate = this.applicationContext.getBean(RestTemplate.class);
        final AtomicBoolean received403 = new AtomicBoolean() ;
        received403.set(false);
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return true
                        ;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
                if (clientHttpResponse.getStatusCode().equals( HttpStatus.FORBIDDEN))
                    received403.set(true);
            }
        });
        restTemplate.getForEntity(authenticationChallenge, Map.class);

        org.junit.Assert.assertTrue(received403 .get() );


    }


}

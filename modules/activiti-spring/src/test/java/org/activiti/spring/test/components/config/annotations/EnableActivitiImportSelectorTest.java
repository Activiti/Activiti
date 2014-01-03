package org.activiti.spring.test.components.config.annotations;

import org.activiti.engine.runtime.Execution;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.EnableActiviti;
import org.activiti.spring.annotations.ProcessHandler;
import org.activiti.spring.annotations.ProcessVariable;
import org.activiti.spring.annotations.State;
import org.activiti.spring.components.config.annotations.ActivitiConfigurer;
import org.activiti.spring.components.config.annotations.EnableActivitiImportSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EnableActivitiConfiguration.class)
public class EnableActivitiImportSelectorTest {


    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void test() {
    }

}


@Component // todo should this be implied in the @ProcessHandler annotation?
@ProcessHandler("fulfillment")
class HanderA {

    @State()
    public void handleAnotherWaitState(Execution execution) {

    }

    @State
    public void handleFulfillmentWaitState(Execution execution, @ProcessVariable long customerId) {
        System.out.println("Handling fulfillment. The customerId process-variable is " + customerId);

    }
}

@Configuration
@EnableActiviti
@ComponentScan
class EnableActivitiConfiguration {


    @Bean
    public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ActivitiConfigurer configurer() {
        return new ActivitiConfigurer() {
            @Override
            public void processDefinitionResources(List<Resource> resourceList) {
            }

            @Override
            public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
                springProcessEngineConfiguration.setDatabaseSchemaUpdate("create-drop");
                springProcessEngineConfiguration.setJobExecutorActivate(true);
            }

            @Override
            public DataSource dataSource() {
                return dataSource();
            }
        };
    }

    @Bean
    public DataSource dataSource() {

        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }
}
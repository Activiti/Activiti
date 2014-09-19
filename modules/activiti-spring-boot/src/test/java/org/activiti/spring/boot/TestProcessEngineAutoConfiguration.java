package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

public class TestProcessEngineAutoConfiguration {

    private ConfigurableApplicationContext applicationContext;

    @Configuration
    @EnableAutoConfiguration
    public static class SimpleDataSourceConfiguration {

        @Bean
        public TaskExecutor taskExecutor() {
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

    @Test
    public void testProcessEngine() throws Exception {
        ProcessEngine processEngine = applicationContext.getBean(ProcessEngine.class);
        Assert.assertNotNull("the processEngine should not be null!", processEngine);
    }

    @After
    public void close() {
        this.applicationContext.close();
    }
    @Test
    public void testRunsWithRestApis() {
        System.setProperty("spring.activiti.restApiEnabled", "true");
        this.applicationContext = SpringApplication.run(SimpleDataSourceConfiguration.class);
        // do RestTemplate or something
    }
    @Test
    public void testRunsWithoutRestApis() {
        System.setProperty("spring.activiti.restApiEnabled", "false");
        this.applicationContext = SpringApplication.run(SimpleDataSourceConfiguration.class);
    }

    @Before
    public void setUp() {

    }

    @Test
    public void testLaunchingProcessDefinition() throws Exception {
        RepositoryService repositoryService = this.applicationContext.getBean(RepositoryService.class);
        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("waiter")
                .list();
        Assert.assertNotNull(processDefinitionList);
        Assert.assertTrue(!processDefinitionList.isEmpty());
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), "waiter");
    }
}

package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
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
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the Spring Integration inbound gateway support.
 *
 * @author Josh Long
 */
public class TestProcessEngineMessaging {

    @Configuration
    @EnableAutoConfiguration
    public static class TestConfig {
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


        @Bean
        ExposeActiviti activitiDelegate(ExposeSpringIntegration exposeSpringIntegration) {
            return new ExposeActiviti(exposeSpringIntegration);
        }

        @Bean
        ExposeSpringIntegration gateway(ProcessEngine processEngine) {
            return new ExposeSpringIntegration(processEngine);
        }

        @Bean
        IntegrationFlow inboundProcess(ExposeSpringIntegration inboundActivitiProcessWaits) {


            return IntegrationFlows.from(inboundActivitiProcessWaits)
                    .handle(new GenericHandler<ActivityExecution>() {
                        @Override
                        public Object handle(ActivityExecution execution, Map<String, Object> headers) {

                            System.out.println("received " + execution.getId() + " with headers " + headers.toString());

                            execution.setVariable("spamId", 123L);

                            return ("Replying to executionID # " + execution.getId());
                        }
                    })
                    .get();
        }
    }

    private ConfigurableApplicationContext applicationContext;

    @After
    public void close() {
        this.applicationContext.close();
    }

    @Before
    public void setUp() {
        this.applicationContext = SpringApplication.run(TestConfig.class);
    }


    /**
     * This should start the ProcessEngine and launch a process that contains a delegate expression.
     * That delegate expression should map to the ActivitiyBehavior that we've built up. It will in turn take the notification that
     * the workflow has arrived at its state and tell Spring Integration about it.
     *
     * @throws Exception
     */
    @Test
    public void testLaunchingGatewayProcessDefinition() throws Exception {
        RepositoryService repositoryService = this.applicationContext.getBean(RepositoryService.class);
        RuntimeService runtimeService = this.applicationContext.getBean(RuntimeService.class);
        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);

        String integrationGatewayProcess = "integrationGatewayProcess";
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(integrationGatewayProcess)
                .list();
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), integrationGatewayProcess);


        // launch a process
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("customerId", 232);

        StopWatch sw = new StopWatch();
        sw.start();
        runtimeService.startProcessInstanceByKey(integrationGatewayProcess, vars);

        sw.stop();
        Thread.sleep(1000 * 5);

    }


}

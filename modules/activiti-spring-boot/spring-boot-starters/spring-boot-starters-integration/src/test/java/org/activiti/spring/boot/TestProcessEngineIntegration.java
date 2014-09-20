package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.integration.ActivitiInboundGateway;
import org.activiti.spring.integration.IntegrationActivityBehavior;
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
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the Spring Integration inbound inboundGateway support.
 *
 * @author Josh Long
 */
public class TestProcessEngineIntegration {

    private ConfigurableApplicationContext applicationContext;

    @After
    public void close() {
        this.applicationContext.close();
    }

    @Before
    public void setUp() {
    }

    void init(Class<?> clzz) {
        this.applicationContext = SpringApplication.run(clzz);
    }

    @Test
    public void testLaunchingGatewayProcessDefinition() throws Exception {
        init(InboundGatewayConfiguration1.class);
        RepositoryService repositoryService = this.applicationContext.getBean(RepositoryService.class);
        RuntimeService runtimeService = this.applicationContext.getBean(RuntimeService.class);
        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);
        String integrationGatewayProcess = "integrationGatewayProcess";
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(integrationGatewayProcess)
                .list();
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), integrationGatewayProcess);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("customerId", 232);
        runtimeService.startProcessInstanceByKey(integrationGatewayProcess, vars);
        System.out.println("finished " + integrationGatewayProcess);
    }

    @Configuration
    @EnableAutoConfiguration
    public static class BaseConfiguration {
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
            basicDataSource.setUrl("jdbc:h2:tcp://localhost/~/activiti");
            //basicDataSource.setUrl("jdbc:h2:mem:activiti");
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


    @Configuration
    @Import(BaseConfiguration.class)
    public static class InboundGatewayConfiguration1 {
        @Bean
        IntegrationActivityBehavior activitiDelegate(ActivitiInboundGateway activitiInboundGateway) {
            return new IntegrationActivityBehavior(activitiInboundGateway);
        }

        @Bean
        ActivitiInboundGateway inboundGateway(ProcessEngine processEngine) {
            return new ActivitiInboundGateway(processEngine, "customerId", "projectId", "orderId");
        }

        @Bean
        IntegrationFlow inboundProcess(ActivitiInboundGateway inboundGateway) {
            return IntegrationFlows
                    .from(inboundGateway)
                    .handle(new GenericHandler<ActivityExecution>() {
                        @Override
                        public Object handle(ActivityExecution execution, Map<String, Object> headers) {
                            return MessageBuilder.withPayload(execution)
                                    .setHeader("projectId", "242453")
                                    .setHeader("orderId", "246")
                                    .copyHeaders(headers).build();
                        }
                    })
                    .get();
        }
    } /// tests that a basic synchronous exchange works. We reply with a payload.


}

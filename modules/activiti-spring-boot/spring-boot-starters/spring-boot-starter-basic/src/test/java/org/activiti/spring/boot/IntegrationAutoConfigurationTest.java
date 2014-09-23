package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.integration.ActivitiInboundGateway;
import org.activiti.spring.integration.IntegrationActivityBehavior;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test the Spring Integration inbound inboundGateway support.
 *
 * @author Josh Long
 */
public class IntegrationAutoConfigurationTest {

    private AnnotationConfigApplicationContext applicationContext;

    @After
    public void close() {
        this.applicationContext.close();
    }


    @Test
    public void testLaunchingGatewayProcessDefinition() throws Exception {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.register(InboundGatewayConfiguration.class);
        this.applicationContext.refresh();


        RepositoryService repositoryService = this.applicationContext.getBean(RepositoryService.class);
        RuntimeService runtimeService = this.applicationContext.getBean(RuntimeService.class);
        ProcessEngine processEngine = this.applicationContext.getBean(ProcessEngine.class);

        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);
        String integrationGatewayProcess = "integrationGatewayProcess";
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(integrationGatewayProcess)
                .list();
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), integrationGatewayProcess);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("customerId", 232);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(integrationGatewayProcess, vars);
        org.junit.Assert.assertTrue(
                this.applicationContext.getBean(InboundGatewayConfiguration.AnalysingService.class)
                        .getStringAtomicReference().get().equals(projectId));
        System.out.println("finished " + integrationGatewayProcess);
    }

    @Configuration
    @Import({DataSourceAutoConfiguration.class,
            DataSourceProcessEngineAutoConfiguration.class,
            IntegrationAutoConfiguration.class})
    public static class BaseConfiguration {

        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        TaskExecutor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }
    }


    @Configuration
    @Import(BaseConfiguration.class)
    public static class InboundGatewayConfiguration {
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
                                    .setHeader("projectId", projectId)
                                    .setHeader("orderId", "246")
                                    .copyHeaders(headers).build();
                        }
                    })
                    .get();
        }


        @Bean
        AnalysingService service() {
            return new AnalysingService();
        }

        public static class AnalysingService {
            private final AtomicReference<String> stringAtomicReference
                    = new AtomicReference<String>();

            public void dump(String projectId) {
                this.stringAtomicReference.set(projectId);
            }

            public AtomicReference<String> getStringAtomicReference() {
                return stringAtomicReference;
            }
        }
    }


    public static final String projectId = "2143243";
}

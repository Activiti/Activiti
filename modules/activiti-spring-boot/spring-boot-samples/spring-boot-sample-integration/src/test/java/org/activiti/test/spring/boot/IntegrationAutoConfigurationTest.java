package org.activiti.test.spring.boot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.activiti.spring.integration.ActivitiInboundGateway;
import org.activiti.spring.integration.IntegrationActivityBehavior;
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

/**
 * Test the Spring Integration inbound inboundGateway support.
 *
 * @author Josh Long
 */
public class IntegrationAutoConfigurationTest {
	
	 @Configuration
   @Import(BaseConfiguration.class)
   public static class InboundGatewayConfiguration {
       @Bean
       public IntegrationActivityBehavior activitiDelegate(ActivitiInboundGateway activitiInboundGateway) {
           return new IntegrationActivityBehavior(activitiInboundGateway);
       }

       @Bean
       public ActivitiInboundGateway inboundGateway(ProcessEngine processEngine) {
           return new ActivitiInboundGateway(processEngine, "customerId", "projectId", "orderId");
       }

       @Bean
       public IntegrationFlow inboundProcess(ActivitiInboundGateway inboundGateway) {
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


       @Bean(name="analysingService")
       public AnalysingService service() {
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

    private AnnotationConfigApplicationContext context(Class<?>... clzz) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(clzz);
        annotationConfigApplicationContext.refresh();
        return annotationConfigApplicationContext;
    }


    @Test
    public void testLaunchingGatewayProcessDefinition() throws Exception {
        AnnotationConfigApplicationContext applicationContext = this.context(InboundGatewayConfiguration.class);
        
        RepositoryService repositoryService = applicationContext.getBean(RepositoryService.class);
        RuntimeService runtimeService = applicationContext.getBean(RuntimeService.class);
        ProcessEngine processEngine = applicationContext.getBean(ProcessEngine.class);

        Assert.assertNotNull("the process engine should not be null", processEngine);
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
        Assert.assertNotNull("the processInstance should not be null", processInstance);
        org.junit.Assert.assertTrue(
                applicationContext.getBean(InboundGatewayConfiguration.AnalysingService.class)
                        .getStringAtomicReference().get().equals(projectId));
    }

    @Configuration
    @Import({DataSourceAutoConfiguration.class,
            DataSourceProcessEngineAutoConfiguration.DataSourceProcessEngineConfiguration.class,
            IntegrationAutoConfiguration.class})
    public static class BaseConfiguration {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public TaskExecutor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }
    }


   
}

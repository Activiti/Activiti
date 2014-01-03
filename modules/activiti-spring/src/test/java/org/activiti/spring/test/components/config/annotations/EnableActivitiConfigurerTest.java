package org.activiti.spring.test.components.config.annotations;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.*;
import org.activiti.spring.components.config.annotations.ActivitiConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EnableActivitiConfigurerTest {

    @Configuration
    @EnableActiviti
    static class EnableActivitiConfiguration {


        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public Handler handerA() {
            return new Handler();
        }

        static class CustomActivitiConfigurer implements ActivitiConfigurer {
            @Override
            public void processDefinitionResources(List<Resource> resourceList) {
                resourceList.add(new ClassPathResource("org/activiti/spring/test/components/waiter.bpmn20.xml"));

                note("processDefinitionResources", true);
            }

            @Override
            public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
                springProcessEngineConfiguration.setDatabaseSchemaUpdate("create-drop");
                springProcessEngineConfiguration.setJobExecutorActivate(true);
                note("postProcessSpringProcessEngineConfiguration", true);
            }

            @Override
            public DataSource dataSource() {
                note("dataSource", true);
                return null;
            }

            private  void note(String p, boolean x) {
                methodMemoryMap.put(p, x);
            }

            public boolean isUsed() {
                return this.methodMemoryMap.size() > 0;
            }

            private final Map<String, Boolean> methodMemoryMap = new ConcurrentHashMap<String, Boolean>();

        }

        @Bean
        public CustomActivitiConfigurer configurer() {
            return new CustomActivitiConfigurer();
        }

        @Bean
        public ProcessStartingClient processStartingClient() {
            return new ProcessStartingClient();
        }

        @Bean
        public DataSource dataSource() {

            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }
    }


    @Before
    public void before() {
        this.customActivitiConfigurer.methodMemoryMap.clear();
    }

    @After
    public void after() {

    }


    @Autowired
    private EnableActivitiConfiguration.CustomActivitiConfigurer customActivitiConfigurer;

    @Test
    public void testDidConfigurerGetUsed() {

        org.junit.Assert.assertTrue("the configurer should have played a part in the configuration process",
                this.customActivitiConfigurer.isUsed());

        org.junit.Assert.assertTrue("processDefinitionResources should have been called and the resulting preferences honored",
                this.customActivitiConfigurer.methodMemoryMap.get("processDefinitionResources"));

        org.junit.Assert.assertTrue("postProcessSpringProcessEngineConfiguration should always be " +
                "called if an ActivitiConfigurer is specified? ",
                this.customActivitiConfigurer.methodMemoryMap.get("postProcessSpringProcessEngineConfiguration"));

    }

    @Autowired
    ProcessStartingClient processStartingClient;

    @Test
    public void testProcessStartingSupport() {


        long customerId = 22;
        ProcessInstance processInstance =
                this.processStartingClient.start(customerId);

        Map<String, Object> procVars = processInstance.getProcessVariables();
        org.junit.Assert.assertTrue(procVars.size() == 1);
        org.junit.Assert.assertTrue(procVars.get("customerId").equals(customerId));

    }

}


class ProcessStartingClient {

    @StartProcess(processKey = "waiter")
    public ProcessInstance start(@ProcessVariable long customerId) {
        System.out.println("starting process with customerId : " + customerId + "");
        return null;
    }


}

@ProcessHandler("fulfillment")
class Handler {

    @State
    public void handleAnotherWaitState(Execution execution) {

    }

    @State
    public void handleFulfillmentWaitState(Execution execution, @ProcessVariable long customerId) {
        System.out.println("Handling fulfillment. The customerId process-variable is " + customerId);


    }
}

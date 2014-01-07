package org.activiti.spring.test.components.config.annotations;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
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
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EnableActivitiConfigurerTest {

    @Autowired
    ProcessStarter processStarter;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    EnableActivitiConfiguration.CustomActivitiConfigurer customActivitiConfigurer;

    @Configuration
    @EnableActiviti
    public static class EnableActivitiConfiguration {

        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public StateHandler handlerA() {
            return new StateHandler();
        }

        public static class CustomActivitiConfigurer implements ActivitiConfigurer {

            private final Map<String, Boolean> methodMemoryMap =
                    new ConcurrentHashMap<String, Boolean>();

            @Override
            public void processDefinitionResources(List<Resource> resourceList) {
                resourceList.add(new ClassPathResource("org/activiti/spring/test/components/waiter.bpmn20.xml"));

                note("processDefinitionResources", true);
            }

            @Override
            public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration configuration) {
                configuration.setDatabaseSchemaUpdate("create-drop");
                configuration.setJobExecutorActivate(true);
                note("postProcessSpringProcessEngineConfiguration", true);
            }

            @Override
            public DataSource dataSource() {
                note("dataSource", true);
                return null;
            }

            private void note(String p, boolean x) {
                this.methodMemoryMap.put(p, x);
            }

            public boolean isUsed() {
                return this.methodMemoryMap.size() > 0;
            }

            public boolean isMethodUsed(String method) {
                return this.methodMemoryMap.containsKey(method) ? this.methodMemoryMap.get(method) : false;
            }

        }

        @Bean
        public CustomActivitiConfigurer configurer() {
            return new CustomActivitiConfigurer();
        }


        @Bean
        public ProcessStarter processStartingClient(ProcessInstance processInstance1) {
            return new ProcessStarter(processInstance1);
        }

        @Bean
        public DataSource dataSource() {

            SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
            simpleDriverDataSource.setDriverClass(org.h2.Driver.class);
            simpleDriverDataSource.setPassword("");
            simpleDriverDataSource.setUrl("sa");
            simpleDriverDataSource.setUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
            return simpleDriverDataSource;
        }
    }


    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testDidConfigurerGetUsed() {
        assertTrue("the configurer should have played a part in the configuration process",
                this.customActivitiConfigurer.isUsed());

        assertTrue("processDefinitionResources should have been called and the resulting preferences honored",
                this.customActivitiConfigurer.isMethodUsed("processDefinitionResources"));

        assertTrue("postProcessSpringProcessEngineConfiguration should always be " +
                "called if an ActivitiConfigurer is specified? ",
                this.customActivitiConfigurer.isMethodUsed("postProcessSpringProcessEngineConfiguration"));
    }

    private Map<String, Object> variables(String id) {
        List<ProcessInstance> processInstances =
                this.runtimeService.createProcessInstanceQuery()
                        .processInstanceId(id)
                        .orderByProcessInstanceId().asc()
                        .list();
        assertTrue("there must be more than one result", processInstances.size() > 0);
        ProcessInstance result = processInstances.iterator().next();
        assertEquals("the executionId's should be the same", result.getId(), id);
        return this.processEngine.getRuntimeService().getVariables(result.getId());
    }


    @Test
    public void testThreadLocalProcessInstance() {
        long customerId = 22;
        Date birthday = new Date();


        assertNotNull("the shared processInstance can't be null",
                this.processStarter.getProcessInstance());

        this.processStarter.enroll(customerId, birthday);


        assertNotNull("the enrollProcessInstanceId can't be null",
                this.processStarter.getEnrollProcessInstanceId());

        List<ProcessInstance> processInstances = this.runtimeService.createProcessInstanceQuery()
                .processInstanceId(this.processStarter.getEnrollProcessInstanceId())
                .orderByProcessInstanceId()
                .asc()
                .list();

        Map<String, Object> vars = this.variables(processInstances.iterator().next().getId());
        assertEquals("the customerId must match the processVariable", (Number) vars.get("customerId"), (Number) customerId);
        assertEquals("the birthday must match the processVariable", (Date) vars.get("birthday"), (Date) birthday);
    }

    @Test
    public void testCorrectProcessVariableInitialization() {
        long customerId = 22;
        ProcessInstance processInstance = this.processStarter.start(customerId);

        Map<String, Object> vars = variables(processInstance.getId());
        assertTrue(vars.size() == 1);
        assertTrue(vars.get("customerId").equals(customerId));
    }


    @Autowired
    ProcessInstance processInstance;


}


class ProcessStarter {

    private ProcessInstance processInstance;
    private String enrollProcessInstanceId;

    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    public String getEnrollProcessInstanceId() {
        return enrollProcessInstanceId;
    }

    public ProcessStarter(ProcessInstance currentProcessInstance) {
        this.processInstance = currentProcessInstance;
    }

    // to a given

    @StartProcess(processKey = "waiter")
    public void enroll(@ProcessVariable long customerId, @ProcessVariable Date birthday) {
        this.enrollProcessInstanceId = this.processInstance.getId();
    }

    @StartProcess(processKey = "waiter")
    public ProcessInstance start(@ProcessVariable long customerId) {
        System.out.println("starting process with customerId : " + customerId + "");
        return null;
    }


}

@ProcessHandler("fulfillment")
class StateHandler {

    @State
    public Future<?> enterWaitStateAndThenSleepAndOnceTheFutureIsReturnedWellSignalContinuation(Execution execution) {
        return null;
    }

    @State
    public void handleAnotherWaitState(Execution execution) {

    }

    @State
    public void handleFulfillmentWaitState(Execution execution, @ProcessVariable long customerId) {
        System.out.println("Handling fulfillment. The customerId process-variable is " + customerId);


    }
}

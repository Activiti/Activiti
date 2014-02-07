package org.activiti.spring.test.components.scope;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.annotations.EnableActiviti;
import org.activiti.spring.annotations.AbstractActivitiConfigurer;
import org.activiti.spring.annotations.ActivitiConfigurer;
import org.h2.Driver;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore // Ignored for the moment. Josh is working on this.
public class EnableActivitiAnnotationProcessScopeTest {

    private ProcessScopeTestEngine processScopeTestEngine;

    @Configuration
    @EnableActiviti
    public static class SimpleConfiguration {


        @Bean
        @Scope("process")
        public StatefulObject statefulObject() {
            return new StatefulObject();
        }

        @Bean
        public Delegate1 delegate1() {
            return new Delegate1();
        }

        @Bean
        public Delegate2 delegate2() {
            return new Delegate2();
        }

        @Bean
        public ActivitiConfigurer activitiConfigurer() {
            return new AbstractActivitiConfigurer() {
                @Override
                public void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
                    springProcessEngineConfiguration.setDatabaseSchemaUpdate("true");
                }

                @Override
                public void processDefinitionResources(List<Resource> resourceList) {
                    resourceList.addAll(
                            this.classPathResourcesForProcessDefinitions(
                                    "org/activiti/spring/test/components/spring-component-waiter.bpmn20.xml"));
                }
            };
        }

        @Bean
        public SimpleDriverDataSource dataSource() {
            return new SimpleDriverDataSource(new Driver(), "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000", "sa", "");
        }
    }

    @Autowired
    public void setProcessEngine(ProcessEngine pe) {
        this.processScopeTestEngine = new ProcessScopeTestEngine(pe);
    }

    @Test
    public void testScopedProxyCreation() throws Throwable {
        this.processScopeTestEngine.testScopedProxyCreation();
    }

}
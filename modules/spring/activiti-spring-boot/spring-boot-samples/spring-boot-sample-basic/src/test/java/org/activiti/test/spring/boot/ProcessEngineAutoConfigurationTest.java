package org.activiti.test.spring.boot;


import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Josh Long
 */
public class ProcessEngineAutoConfigurationTest {

    @Test
    public void processEngineWithBasicDataSource() throws Exception {
        AnnotationConfigApplicationContext context = this.context(
                DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceProcessEngineConfiguration.class);
        Assert.assertNotNull("the processEngine should not be null!", context.getBean(ProcessEngine.class));
    }

    @Test
    public void launchProcessDefinition() throws Exception {
        AnnotationConfigApplicationContext applicationContext = this.context(
                DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceProcessEngineConfiguration.class);
        RepositoryService repositoryService = applicationContext.getBean(RepositoryService.class);
        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);
        Assert.assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("waiter")
                .list();
        Assert.assertNotNull(processDefinitionList);
        Assert.assertTrue(!processDefinitionList.isEmpty());
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals("waiter", processDefinition.getKey());
    }

    private AnnotationConfigApplicationContext context(Class<?>... clzz) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(clzz);
        annotationConfigApplicationContext.refresh();
        return annotationConfigApplicationContext;
    }
}

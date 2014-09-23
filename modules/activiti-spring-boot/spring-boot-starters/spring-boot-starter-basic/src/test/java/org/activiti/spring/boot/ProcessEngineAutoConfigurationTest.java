package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * @author Josh Long
 */
public class ProcessEngineAutoConfigurationTest {

    private AnnotationConfigApplicationContext applicationContext;

    @Test
    public void processEngineWithBasicDataSource() throws Exception {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.register(DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.class);
        this.applicationContext.refresh();
        ProcessEngine processEngine = applicationContext.getBean(ProcessEngine.class);
        Assert.assertNotNull("the processEngine should not be null!", processEngine);
    }

    @Test
    public void launchProcessDefinition() throws Exception {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.register(DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.class);
        this.applicationContext.refresh();
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

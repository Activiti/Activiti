package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.persistence.EntityManagerFactory;
import java.util.List;

/**
 * @author Josh Long
 */
public class ProcessEngineAutoConfigurationTest {

    @Test
    public void processEngineWithJpaEntityManager() throws Exception {
        AnnotationConfigApplicationContext context = this.context(DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class, JpaProcessEngineAutoConfiguration.JpaConfiguration.class);
        Assert.assertNotNull("entityManagerFactory should not be null", context.getBean(EntityManagerFactory.class));
        Assert.assertNotNull("the processEngine should not be null!", context.getBean(ProcessEngine.class));
        SpringProcessEngineConfiguration configuration = context.getBean(SpringProcessEngineConfiguration.class);
        Assert.assertNotNull("the " + SpringProcessEngineConfiguration.class.getName() + " should not be null", configuration);
        Assert.assertNotNull(configuration.getJpaEntityManagerFactory());
    }

    @Test
    public void processEngineWithBasicDataSource() throws Exception {
        AnnotationConfigApplicationContext context = this.context(
                DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class);
        Assert.assertNotNull("the processEngine should not be null!", context.getBean(ProcessEngine.class));
    }

    @Test
    public void launchProcessDefinition() throws Exception {
        AnnotationConfigApplicationContext applicationContext = this.context(
                DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class);
        RepositoryService repositoryService = applicationContext.getBean(RepositoryService.class);
        Assert.assertNotNull("we should have a default repositoryService included", repositoryService);
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("waiter")
                .list();
        Assert.assertNotNull(processDefinitionList);
        Assert.assertTrue(!processDefinitionList.isEmpty());
        ProcessDefinition processDefinition = processDefinitionList.iterator().next();
        Assert.assertEquals(processDefinition.getKey(), "waiter");
    }

    private AnnotationConfigApplicationContext context(Class<?>... clzz) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(clzz);
        annotationConfigApplicationContext.refresh();
        return annotationConfigApplicationContext;
    }
}

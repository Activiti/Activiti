package org.activiti.test.spring.boot;


import javax.persistence.EntityManagerFactory;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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


    private AnnotationConfigApplicationContext context(Class<?>... clzz) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(clzz);
        annotationConfigApplicationContext.refresh();
        return annotationConfigApplicationContext;
    }
}

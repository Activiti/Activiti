package org.activiti.spring.boot;

import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Joram Barrez
 * @author Josh Long
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(DataSourceProcessEngineAutoConfiguration.class)
@ConditionalOnClass(name = "javax.persistence.EntityManagerFactory")
public class JpaProcessEngineAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(ActivitiProperties.class)
    public static class JpaConfiguration
            extends AbstractProcessEngineAutoConfiguration {


        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        @ConditionalOnMissingBean
        public SpringProcessEngineConfiguration springProcessEngineConfiguration(
                DataSource dataSource, EntityManagerFactory entityManagerFactory,
                PlatformTransactionManager transactionManager, SpringJobExecutor springJobExecutor) throws IOException {

            SpringProcessEngineConfiguration config = this.baseSpringProcessEngineConfiguration(dataSource, transactionManager, springJobExecutor);
            config.setJpaEntityManagerFactory(entityManagerFactory);
            config.setTransactionManager(transactionManager);
            config.setJpaHandleTransaction(false);
            config.setJpaCloseEntityManager(false);
            return config;
        }
    }

}

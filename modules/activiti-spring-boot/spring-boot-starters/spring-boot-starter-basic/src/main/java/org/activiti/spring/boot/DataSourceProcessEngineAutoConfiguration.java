package org.activiti.spring.boot;

import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Joram Barrez
 * @author Josh Long
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnMissingClass(name = "javax.persistence.EntityManagerFactory")
public class DataSourceProcessEngineAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(ActivitiProperties.class)
    public static class DataSourceConfiguration
            extends AbstractProcessEngineAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        @ConditionalOnMissingBean
        public SpringProcessEngineConfiguration springProcessEngineConfiguration(
                DataSource dataSource,
                PlatformTransactionManager transactionManager,
                SpringJobExecutor springJobExecutor) throws IOException {
          
            return this.baseSpringProcessEngineConfiguration(
                    dataSource, transactionManager, springJobExecutor);
        }
    }

}

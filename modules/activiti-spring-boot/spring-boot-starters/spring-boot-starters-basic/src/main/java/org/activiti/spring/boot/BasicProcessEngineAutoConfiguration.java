/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot;

import org.activiti.engine.*;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringJobExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * Auto configuration for using Activiti from a <a
 * href="http://spring.io/projects/spring-boot">Spring Boot application</a>.
 * Provides a configured {@link org.activiti.engine.ProcessEngine} if none other
 * is detected.
 * <p>
 * Discovers any process definitions deployed in the
 * {@literal src/main/resources/process} folder, and uses the single
 * {@link javax.sql.DataSource} bean discovered in the Spring application
 * context..
 *
 * @author Josh Long
 * @author Joram Barrez
 */
@EnableConfigurationProperties(ActivitiProperties.class)
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class BasicProcessEngineAutoConfiguration {

    @Configuration
    public static class DefaultProcessEngineConfiguration
            extends AbstractProcessEngineConfiguration   {

        @Bean
        public SpringJobExecutor springJobExecutor(TaskExecutor taskExecutor) {
            return new SpringJobExecutor(taskExecutor);
        }

        @Autowired
        private ActivitiProperties activitiProperties;

        @Autowired
        private ResourcePatternResolver resourceLoader;

        @Bean
        @ConditionalOnBean(DataSource.class)
        public SpringProcessEngineConfiguration activitiConfiguration(
                DataSource dataSource, PlatformTransactionManager transactionManager, SpringJobExecutor springJobExecutor) throws IOException {
            List<Resource> procDefResources = this.discoverProcessDefinitionResources(
                    this.resourceLoader, this.activitiProperties.getProcessDefinitionLocationPrefix(),
                    this.activitiProperties.getProcessDefinitionLocationSuffix(),
                    this.activitiProperties.isCheckProcessDefinitions());


            SpringProcessEngineConfiguration conf = super.processEngineConfigurationBean(
                    procDefResources.toArray(new Resource[procDefResources.size()]), dataSource, transactionManager, springJobExecutor);

            conf.setDeploymentName(defaultText(
                    activitiProperties.getDeploymentName(),
                    conf.getDeploymentName()));

            conf.setDatabaseSchema(defaultText(
                    activitiProperties.getDatabaseSchema(),
                    conf.getDatabaseSchema()));

            conf.setDatabaseSchemaUpdate(defaultText(
                    activitiProperties.getDatabaseSchemaUpdate(),
                    conf.getDatabaseSchemaUpdate()));

            return conf;
        }

        private String defaultText(String deploymentName, String deploymentName1) {
            if (StringUtils.hasText(deploymentName))
                return deploymentName;
            return deploymentName1;
        }

        @Bean
        public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration configuration) throws Exception {
            ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
            processEngineFactoryBean.setProcessEngineConfiguration(configuration);
            return processEngineFactoryBean;
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public RuntimeService runtimeServiceBean(ProcessEngine processEngine) {
            return super.runtimeServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public RepositoryService repositoryServiceBean(ProcessEngine processEngine) {
            return super.repositoryServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public TaskService taskServiceBean(ProcessEngine processEngine) {
            return super.taskServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public HistoryService historyServiceBean(ProcessEngine processEngine) {
            return super.historyServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public ManagementService managementServiceBeanBean(ProcessEngine processEngine) {
            return super.managementServiceBeanBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public FormService formServiceBean(ProcessEngine processEngine) {
            return super.formServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        @Override
        public IdentityService identityServiceBean(ProcessEngine processEngine) {
            return super.identityServiceBean(processEngine);
        }

        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        @ConditionalOnMissingBean
        public TaskExecutor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

    }

/*

*/


/*
    @Configuration
    @ConditionalOnClass({EntityManagerFactory.class})
    public static class JpaAutoConfiguration {

        @Bean
        InitializingBean configureJpaForActiviti(
                final EntityManagerFactory emf,
                final ActivitiProperties activitiProperties,
                final SpringProcessEngineConfiguration processEngineAutoConfiguration) {
            return new InitializingBean() {

                @Override
                public void afterPropertiesSet() throws Exception {
                    if (activitiProperties.isJpa()) {
                        processEngineAutoConfiguration
                                .setJpaEntityManagerFactory(emf);
                        processEngineAutoConfiguration
                                .setJpaHandleTransaction(false);
                        processEngineAutoConfiguration
                                .setJpaCloseEntityManager(false);
                    }
                }
            };
        }

    }*/
}


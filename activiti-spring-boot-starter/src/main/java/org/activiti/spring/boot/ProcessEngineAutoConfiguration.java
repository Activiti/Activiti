/* Licensed under the Apache License, Version 2.0 (the "License");
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

import org.activiti.api.process.model.events.ProcessDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.spring.ProcessDeployedEventProducer;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.process.validation.AsyncPropertyValidator;
import org.activiti.spring.bpmn.parser.CloudActivityBehaviorFactory;
import org.activiti.validation.ProcessValidatorImpl;
import org.activiti.validation.validator.ValidatorSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Configuration
@AutoConfigureAfter(name = {"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration"})
@EnableConfigurationProperties(ActivitiProperties.class)
public class ProcessEngineAutoConfiguration extends AbstractProcessEngineAutoConfiguration {

    private final UserGroupManager userGroupManager;

    public ProcessEngineAutoConfiguration(UserGroupManager userGroupManager) {
        this.userGroupManager = userGroupManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
            SpringAsyncExecutor springAsyncExecutor,
            ActivitiProperties activitiProperties,
            ProcessDefinitionResourceFinder processDefinitionResourceFinder,
            @Autowired(required = false) ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer,
            @Autowired(required = false) List<ProcessEngineConfigurator> processEngineConfigurators) throws IOException {

        SpringProcessEngineConfiguration conf = new SpringProcessEngineConfiguration();
        conf.setConfigurators(processEngineConfigurators);
        configureProcessDefinitionResources(processDefinitionResourceFinder,
                conf);
        conf.setDataSource(dataSource);
        conf.setTransactionManager(transactionManager);

        if (springAsyncExecutor != null) {
            conf.setAsyncExecutor(springAsyncExecutor);
        }
        conf.setDeploymentName(activitiProperties.getDeploymentName());
        conf.setDatabaseSchema(activitiProperties.getDatabaseSchema());
        conf.setDatabaseSchemaUpdate(activitiProperties.getDatabaseSchemaUpdate());
        conf.setDbHistoryUsed(activitiProperties.isDbHistoryUsed());
        conf.setAsyncExecutorActivate(activitiProperties.isAsyncExecutorActivate());
        if (!activitiProperties.isAsyncExecutorActivate()) {
            ValidatorSet springBootStarterValidatorSet = new ValidatorSet("activiti-spring-boot-starter");
            springBootStarterValidatorSet.addValidator(new AsyncPropertyValidator());
            if (conf.getProcessValidator() == null) {
                ProcessValidatorImpl processValidator = new ProcessValidatorImpl();
                processValidator.addValidatorSet(springBootStarterValidatorSet);
                conf.setProcessValidator(processValidator);
            } else {
                conf.getProcessValidator().getValidatorSets().add(springBootStarterValidatorSet);
            }
        }
        conf.setMailServerHost(activitiProperties.getMailServerHost());
        conf.setMailServerPort(activitiProperties.getMailServerPort());
        conf.setMailServerUsername(activitiProperties.getMailServerUserName());
        conf.setMailServerPassword(activitiProperties.getMailServerPassword());
        conf.setMailServerDefaultFrom(activitiProperties.getMailServerDefaultFrom());
        conf.setMailServerUseSSL(activitiProperties.isMailServerUseSsl());
        conf.setMailServerUseTLS(activitiProperties.isMailServerUseTls());

        if (userGroupManager != null) {
            conf.setUserGroupManager(userGroupManager);
        }

        conf.setHistoryLevel(activitiProperties.getHistoryLevel());
        conf.setCopyVariablesToLocalForTasks(activitiProperties.isCopyVariablesToLocalForTasks());
        conf.setSerializePOJOsInVariablesToJson(activitiProperties.isSerializePOJOsInVariablesToJson());
        conf.setJavaClassFieldForJackson(activitiProperties.getJavaClassFieldForJackson());

        if (activitiProperties.getCustomMybatisMappers() != null) {
            conf.setCustomMybatisMappers(getCustomMybatisMapperClasses(activitiProperties.getCustomMybatisMappers()));
        }

        if (activitiProperties.getCustomMybatisXMLMappers() != null) {
            conf.setCustomMybatisXMLMappers(new HashSet<>(activitiProperties.getCustomMybatisXMLMappers()));
        }

        if (activitiProperties.getCustomMybatisXMLMappers() != null) {
            conf.setCustomMybatisXMLMappers(new HashSet<>(activitiProperties.getCustomMybatisXMLMappers()));
        }

        if (activitiProperties.isUseStrongUuids()) {
            conf.setIdGenerator(new StrongUuidGenerator());
        }

        if (activitiProperties.getDeploymentMode() != null) {
            conf.setDeploymentMode(activitiProperties.getDeploymentMode());
        }

        conf.setActivityBehaviorFactory(new CloudActivityBehaviorFactory());

        if (processEngineConfigurationConfigurer != null) {
            processEngineConfigurationConfigurer.configure(conf);
        }

        return conf;
    }

    private void configureProcessDefinitionResources(ProcessDefinitionResourceFinder processDefinitionResourceFinder,
                                                     SpringProcessEngineConfiguration conf) throws IOException {
        List<Resource> procDefResources = processDefinitionResourceFinder.discoverProcessDefinitionResources();
        if (!procDefResources.isEmpty()) {
            conf.setDeploymentResources(procDefResources.toArray(new Resource[0]));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessDefinitionResourceFinder processDefinitionResourceFinder(ActivitiProperties activitiProperties,
                                                                           ResourcePatternResolver resourcePatternResolver) {
        return new ProcessDefinitionResourceFinder(activitiProperties,
                resourcePatternResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessDeployedEventProducer processDeployedEventProducer(RepositoryService repositoryService,
                                                                     APIProcessDefinitionConverter converter,
                                                                     @Autowired(required = false) List<ProcessRuntimeEventListener<ProcessDeployedEvent>> listeners,
                                                                     ApplicationEventPublisher eventPublisher) {
        return new ProcessDeployedEventProducer(repositoryService,
                                                converter,
                                                Optional.ofNullable(listeners)
                                                        .orElse(Collections.emptyList()),
                                                eventPublisher);
    }
}


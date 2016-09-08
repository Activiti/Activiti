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
package org.activiti.app.conf;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.configurator.DmnEngineConfigurator;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.runtime.Clock;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.configurator.FormEngineConfigurator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan(basePackages= {
		"org.activiti.app.runtime.activiti",
		"org.activiti.app.extension.conf", // For custom configuration classes
		"org.activiti.app.extension.bean" // For custom beans (delegates etc.)
})
public class ActivitiEngineConfiguration {

    private final Logger logger = LoggerFactory.getLogger(ActivitiEngineConfiguration.class);
    
    @Inject
    private DataSource dataSource;
    
    @Inject
    private PlatformTransactionManager transactionManager;
    
    @Inject
    private Environment environment;
    
    @Bean(name="processEngine")
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }
    
    public ProcessEngine processEngine() {
        // Safe to call the getObject() on the @Bean annotated processEngineFactoryBean(), will be
        // the fully initialized object instanced from the factory and will NOT be created more than once
        try {
            return processEngineFactoryBean().getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Bean(name="processEngineConfiguration")
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
    	SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
    	processEngineConfiguration.setDataSource(dataSource);
    	processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    	processEngineConfiguration.setTransactionManager(transactionManager);
    	processEngineConfiguration.setAsyncExecutorActivate(true);
    	processEngineConfiguration.setAsyncExecutor(asyncExecutor());

    	String emailHost = environment.getProperty("email.host");
    	if (StringUtils.isNotEmpty(emailHost)) {
        	processEngineConfiguration.setMailServerHost(emailHost);
        	processEngineConfiguration.setMailServerPort(environment.getRequiredProperty("email.port", Integer.class));
        	
        	Boolean useCredentials = environment.getProperty("email.useCredentials", Boolean.class);
            if (Boolean.TRUE.equals(useCredentials)) {
                processEngineConfiguration.setMailServerUsername(environment.getProperty("email.username"));
                processEngineConfiguration.setMailServerPassword(environment.getProperty("email.password"));
            }
    	}
    	
    	// Limit process definition cache
    	processEngineConfiguration.setProcessDefinitionCacheLimit(environment.getProperty("activiti.process-definitions.cache.max", Integer.class, 128));
    	
    	// Enable safe XML. See http://activiti.org/userguide/index.html#advanced.safe.bpmn.xml
    	processEngineConfiguration.setEnableSafeBpmnXml(true);
    	
    	List<BpmnParseHandler> preParseHandlers = new ArrayList<BpmnParseHandler>();
    	processEngineConfiguration.setPreBpmnParseHandlers(preParseHandlers);
    	
    	FormEngineConfiguration formEngineConfiguration = new FormEngineConfiguration();
    	formEngineConfiguration.setDataSource(dataSource);
    	
    	FormEngineConfigurator formEngineConfigurator = new FormEngineConfigurator();
    	formEngineConfigurator.setFormEngineConfiguration(formEngineConfiguration);
    	processEngineConfiguration.addConfigurator(formEngineConfigurator);
    	
    	DmnEngineConfiguration dmnEngineConfiguration = new DmnEngineConfiguration();
    	dmnEngineConfiguration.setDataSource(dataSource);
      
      DmnEngineConfigurator dmnEngineConfigurator = new DmnEngineConfigurator();
      dmnEngineConfigurator.setDmnEngineConfiguration(dmnEngineConfiguration);
      processEngineConfiguration.addConfigurator(dmnEngineConfigurator);
    	
    	return processEngineConfiguration;
    }
    
    @Bean
    public AsyncExecutor asyncExecutor() {
        DefaultAsyncJobExecutor asyncExecutor = new DefaultAsyncJobExecutor();
        asyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(5000);
        asyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(5000);
        return asyncExecutor;
    }
    
    @Bean(name="clock")
    @DependsOn("processEngine")
    public Clock getClock() {
    	return processEngineConfiguration().getClock();
    }
    
    @Bean
    public RepositoryService repositoryService() {
    	return processEngine().getRepositoryService();
    }
    
    @Bean
    public RuntimeService runtimeService() {
    	return processEngine().getRuntimeService();
    }
    
    @Bean
    public TaskService taskService() {
    	return processEngine().getTaskService();
    }
    
    @Bean
    public HistoryService historyService() {
    	return processEngine().getHistoryService();
    }
    
    @Bean
    public FormService formService() {
    	return processEngine().getFormService();
    }
    
    @Bean
    public IdentityService identityService() {
    	return processEngine().getIdentityService();
    }
    
    @Bean
    public ManagementService managementService() {
    	return processEngine().getManagementService();
    }
    
    @Bean
    public FormRepositoryService formEngineRepositoryService() {
      return processEngine().getFormEngineRepositoryService();
    }
    
    @Bean
    public org.activiti.form.api.FormService formEngineFormService() {
      return processEngine().getFormEngineFormService();
    }
}

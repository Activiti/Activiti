/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.conf;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

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
		"com.activiti.runtime.activiti",
		"com.activiti.extension.conf", // For custom configuration classes
		"com.activiti.extension.bean" // For custom beans (delegates etc.)
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
}

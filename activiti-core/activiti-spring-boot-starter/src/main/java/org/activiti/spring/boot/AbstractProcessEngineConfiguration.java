/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.boot;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextManager;
import org.activiti.engine.integration.IntegrationContextService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;

/**
 * Provides sane definitions for the various beans required to be productive with Activiti in Spring.
 *
 */
public abstract class AbstractProcessEngineConfiguration {

  public ProcessEngineFactoryBean springProcessEngineBean(SpringProcessEngineConfiguration configuration) {
    ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
    processEngineFactoryBean.setProcessEngineConfiguration(configuration);
    return processEngineFactoryBean;
  }

  public RuntimeService runtimeServiceBean(ProcessEngine processEngine) {
    return processEngine.getRuntimeService();
  }

  public RepositoryService repositoryServiceBean(ProcessEngine processEngine) {
    return processEngine.getRepositoryService();
  }

  public TaskService taskServiceBean(ProcessEngine processEngine) {
    return processEngine.getTaskService();
  }

  public HistoryService historyServiceBean(ProcessEngine processEngine) {
    return processEngine.getHistoryService();
  }

  public ManagementService managementServiceBeanBean(ProcessEngine processEngine) {
    return processEngine.getManagementService();
  }

  public IntegrationContextManager integrationContextManagerBean(ProcessEngine processEngine) {
      return processEngine.getProcessEngineConfiguration().getIntegrationContextManager();
  }

  public IntegrationContextService integrationContextServiceBean(ProcessEngine processEngine) {
      return processEngine.getProcessEngineConfiguration().getIntegrationContextService();
  }

}

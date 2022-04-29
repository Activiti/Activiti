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

package org.activiti.engine.impl.scripting;

import static java.util.Arrays.asList;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


public class VariableScopeResolver implements Resolver {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected VariableScope variableScope;

  protected String variableScopeKey = "execution";

  protected static final String processEngineConfigurationKey = "processEngineConfiguration";
  protected static final String runtimeServiceKey = "runtimeService";
  protected static final String taskServiceKey = "taskService";
  protected static final String repositoryServiceKey = "repositoryService";
  protected static final String managementServiceKey = "managementService";
  protected static final String historyServiceKey = "historyService";
  protected static final String formServiceKey = "formService";

  protected static final List<String> KEYS = asList(
      processEngineConfigurationKey, runtimeServiceKey, taskServiceKey,
      repositoryServiceKey, managementServiceKey, historyServiceKey, formServiceKey);


  public VariableScopeResolver(ProcessEngineConfigurationImpl processEngineConfiguration, VariableScope variableScope) {

    this.processEngineConfiguration = processEngineConfiguration;

    if (variableScope == null) {
      throw new ActivitiIllegalArgumentException("variableScope cannot be null");
    }
    if (variableScope instanceof ExecutionEntity) {
      variableScopeKey = "execution";
    } else if (variableScope instanceof TaskEntity) {
      variableScopeKey = "task";
    } else {
      throw new ActivitiException("unsupported variable scope type: " + variableScope.getClass().getName());
    }
    this.variableScope = variableScope;
  }

  public boolean containsKey(Object key) {
    return variableScopeKey.equals(key) || KEYS.contains(key)|| variableScope.hasVariable((String) key);
  }

  public Object get(Object key) {
    if (variableScopeKey.equals(key)) {
      return variableScope;
    } else if (processEngineConfigurationKey.equals(key)) {
      return processEngineConfiguration;
    } else if (runtimeServiceKey.equals(key)) {
      return processEngineConfiguration.getRuntimeService();
    } else if (taskServiceKey.equals(key)) {
      return processEngineConfiguration.getTaskService();
    } else if (repositoryServiceKey.equals(key)) {
      return processEngineConfiguration.getRepositoryService();
    } else if (managementServiceKey.equals(key)) {
      return processEngineConfiguration.getManagementService();
    }

    return variableScope.getVariable((String) key);
  }
}

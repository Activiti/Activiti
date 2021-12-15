/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Map;

import org.activiti.engine.impl.ProcessInstanceServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilder;
import org.activiti.engine.impl.util.ProcessDefinitionRetriever;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class StartProcessInstanceCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;
    private final ProcessInstanceBuilder processInstanceBuilder;
    private final ProcessInstanceServiceImpl processInstanceService;
    protected ProcessInstanceHelper processInstanceHelper;

    public StartProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder) {
        this(processInstanceBuilder, new ProcessInstanceServiceImpl());
    }

    public StartProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder, ProcessInstanceServiceImpl processInstanceService) {
        this.processInstanceBuilder = processInstanceBuilder;
        this.processInstanceService = processInstanceService;
    }

    public ProcessInstance execute(CommandContext commandContext) {
      DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentManager();

      ProcessDefinitionRetriever processRetriever = new ProcessDefinitionRetriever(processInstanceBuilder.getTenantId(), deploymentCache);
      ProcessDefinition processDefinition = processRetriever.getProcessDefinition(processInstanceBuilder.getProcessDefinitionId(), processInstanceBuilder.getProcessDefinitionKey());

      processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
    return createAndStartProcessInstance(
        processDefinition,
        processInstanceBuilder.getBusinessKey(),
        processInstanceBuilder.getProcessInstanceName(),
        processInstanceBuilder.getVariables(),
        processInstanceBuilder.getTransientVariables());
  }

  protected ProcessInstance createAndStartProcessInstance(
      ProcessDefinition processDefinition,
      String businessKey,
      String processInstanceName,
      Map<String,Object> variables,
      Map<String, Object> transientVariables) {

    return processInstanceService.createAndStartProcessInstance(
        processDefinition, businessKey, processInstanceName, variables, transientVariables);
  }

}

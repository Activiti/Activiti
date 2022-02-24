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

import org.activiti.engine.impl.ProcessInstanceServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilder;
import org.activiti.engine.impl.util.ProcessDefinitionRetriever;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class CreateProcessInstanceCmd implements Command<ProcessInstance> {

    private final ProcessInstanceBuilder processInstanceBuilder;
    private final ProcessInstanceServiceImpl processInstanceService;

    public CreateProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder) {
        this(processInstanceBuilder, new ProcessInstanceServiceImpl());
    }

    public CreateProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder, ProcessInstanceServiceImpl processInstanceService) {
        this.processInstanceBuilder = processInstanceBuilder;
        this.processInstanceService = processInstanceService;
    }

    public ProcessInstance execute(CommandContext commandContext) {

        DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration()
            .getDeploymentManager();

        ProcessDefinitionRetriever processRetriever = new ProcessDefinitionRetriever(
            processInstanceBuilder.getTenantId(), deploymentCache);
        ProcessDefinition processDefinition = processRetriever.getProcessDefinition(
            processInstanceBuilder.getProcessDefinitionId(),
            processInstanceBuilder.getProcessDefinitionKey());

        return processInstanceService.createProcessInstance(
            processDefinition,
            processInstanceBuilder.getBusinessKey(),
            processInstanceBuilder.getProcessInstanceName());
    }


}

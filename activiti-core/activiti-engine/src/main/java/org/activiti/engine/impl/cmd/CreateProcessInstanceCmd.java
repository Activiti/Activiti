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

import static org.activiti.engine.impl.util.ProcessDefinitionUtil.getProcess;
import static org.activiti.engine.impl.util.ProcessDefinitionUtil.getProcessDefinitionFromDatabase;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilder;
import org.activiti.engine.impl.util.ProcessDefinitionRetriever;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class CreateProcessInstanceCmd implements Command<ProcessInstance> {

    private final ProcessInstanceBuilder processInstanceBuilder;

    public CreateProcessInstanceCmd(ProcessInstanceBuilder processInstanceBuilder) {
        this.processInstanceBuilder = processInstanceBuilder;
    }

    public ProcessInstance execute(CommandContext commandContext) {

        DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration()
            .getDeploymentManager();

        ProcessDefinitionRetriever processRetriever = new ProcessDefinitionRetriever(
            processInstanceBuilder.getTenantId(), deploymentCache);
        ProcessDefinition processDefinition = processRetriever.getProcessDefinition(
            processInstanceBuilder.getProcessDefinitionId(),
            processInstanceBuilder.getProcessDefinitionKey());

        return createProcessInstance(
            processDefinition,
            processInstanceBuilder.getBusinessKey(),
            processInstanceBuilder.getProcessInstanceName());
    }

    public ProcessInstance createProcessInstance(
        ProcessDefinition processDefinition,
        String businessKey,
        String processInstanceName) {

        Process process = getActiveProcess(processDefinition);

        FlowElement initialFlowElement = getInitialFlowElement(process, processDefinition.getId());

        return createProcessInstanceWithInitialFlowElement(
            processDefinition,
            businessKey,
            processInstanceName,
            initialFlowElement);
    }

    public Process getActiveProcess(ProcessDefinition processDefinition) {
        ProcessDefinitionEntity processDefinitionEntity =
            getProcessDefinitionFromDatabase(processDefinition.getId());

        if (processDefinitionEntity.isSuspended()) {
            throw new ActivitiException(processSuspendedMessage(processDefinition));
        }

        Process process = getProcess(processDefinitionEntity);

        if (process == null) {
            throw new ActivitiException(processNotFoundMessage(processDefinition));
        }

        return process;
    }

    public FlowElement getInitialFlowElement(Process process, String processDefinitionId) {
        FlowElement initialFlowElement = process.getInitialFlowElement();

        if (initialFlowElement == null) {
            throw new ActivitiException(noStartElementMessage(processDefinitionId));
        }

        return initialFlowElement;
    }

    private String noStartElementMessage(String processDefinitionId) {
        return "No start element found for process definition " + processDefinitionId;
    }

    public ExecutionEntity createProcessInstanceWithInitialFlowElement(
        ProcessDefinition processDefinition,
        String businessKey,
        String processInstanceName,
        FlowElement initialFlowElement) {

        CommandContext commandContext = Context.getCommandContext();

        ExecutionEntity processInstance = createProcessInstanceFromExecutionEntityManager(
            processDefinition, businessKey, initialFlowElement, commandContext);

        setProcessInstanceNameInHistory(processInstanceName, commandContext, processInstance);

        createProcessExecutionChildren(initialFlowElement, commandContext, processInstance);

        return processInstance;
    }

    /**
     * Create the first execution that will visit all the process definition elements
     * @param initialFlowElement
     * @param commandContext
     * @param processInstance
     */
    private void createProcessExecutionChildren(FlowElement initialFlowElement, CommandContext commandContext,
        ExecutionEntity processInstance) {
        ExecutionEntity execution = commandContext.getExecutionEntityManager()
            .createChildExecution(processInstance);

        execution.setCurrentFlowElement(initialFlowElement);
    }

    private void setProcessInstanceNameInHistory(String processInstanceName, CommandContext commandContext,
        ExecutionEntity processInstance) {
        // Set processInstance name
        if (processInstanceName != null) {
            processInstance.setName(processInstanceName);
            commandContext.getHistoryManager()
                .recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
        }
    }

    private ExecutionEntity createProcessInstanceFromExecutionEntityManager(ProcessDefinition processDefinition,
        String businessKey, FlowElement initialFlowElement, CommandContext commandContext) {
        // Create the process instance
        return commandContext.getExecutionEntityManager()
            .createProcessInstanceExecution(
                processDefinition,
                businessKey,
                processDefinition.getTenantId(),
                getInitiatorVariableName(initialFlowElement));
    }

    private String getInitiatorVariableName(FlowElement initialFlowElement) {
        if (!(initialFlowElement instanceof StartEvent)) {
            return null;
        }

        return ((StartEvent) initialFlowElement).getInitiator();
    }

    private String processSuspendedMessage(ProcessDefinition processDefinition) {
        return "Cannot start process instance. Process definition " + processDefinition.getName()
            + " (id = " + processDefinition.getId() + ") is suspended";
    }

    private String processNotFoundMessage(ProcessDefinition processDefinition) {
        return "Cannot start process instance. Process model " + processDefinition.getName()
            + " (id = " + processDefinition.getId() + ") could not be found";
    }

}

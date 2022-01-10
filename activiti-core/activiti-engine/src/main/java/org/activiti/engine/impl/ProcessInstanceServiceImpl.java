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
package org.activiti.engine.impl;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.activiti.engine.delegate.event.ActivitiEventType.ENTITY_INITIALIZED;
import static org.activiti.engine.delegate.event.impl.ActivitiEventBuilder.createEntityWithVariablesEvent;
import static org.activiti.engine.impl.util.ProcessDefinitionUtil.getBpmnModel;
import static org.activiti.engine.impl.util.ProcessDefinitionUtil.getProcess;
import static org.activiti.engine.impl.util.ProcessDefinitionUtil.getProcessDefinitionFromDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * This service expose the methods about ProcessInstance lifecycle.
 */
public class ProcessInstanceServiceImpl {

    /**
     * Creates a processInstance and starts it.
     *
     * @param processDefinition the process definition
     * @param businessKey the string representing the business key
     * @param processInstanceName name of the processInstance
     * @param variables process variables
     * @param transientVariables process transient variables
     * @return the created and started process instance
     */
    public ProcessInstance createAndStartProcessInstance(
        ProcessDefinition processDefinition,
        String businessKey,
        String processInstanceName,
        Map<String, Object> variables,
        Map<String, Object> transientVariables) {

        CommandContext commandContext = Objects.requireNonNull(Context.getCommandContext());

        Process process = getActiveProcess(processDefinition);
        FlowElement initialFlowElement = getInitialFlowElement(process, processDefinition.getId());

        ExecutionEntity processInstance = createProcessInstanceFromExecutionEntityManager(
            commandContext, processDefinition, businessKey, initialFlowElement);

        setProcessInstanceNameInHistory(commandContext, processInstanceName, processInstance);
        createProcessExecutionChildren(commandContext, initialFlowElement, processInstance);

        startProcessInstance(
            commandContext,
            processInstance,
            process,
            variables,
            transientVariables,
            initialFlowElement);

        return processInstance;
    }

    /**
     * Create a ProcessInstance.
     *
     * @param processDefinition the process definition instance
     * @param businessKey the businessKey
     * @param processInstanceName the process instance name
     * @return the instantiated ProcessInstance
     */
    public ExecutionEntity createProcessInstance(
        ProcessDefinition processDefinition,
        String businessKey,
        String processInstanceName) {

        CommandContext commandContext = Objects.requireNonNull(Context.getCommandContext());

        Process process = getActiveProcess(processDefinition);
        FlowElement initialFlowElement = getInitialFlowElement(process, processDefinition.getId());

        ExecutionEntity processInstance = createProcessInstanceFromExecutionEntityManager(
            commandContext, processDefinition, businessKey, initialFlowElement);

        setProcessInstanceNameInHistory(commandContext, processInstanceName, processInstance);
        createProcessExecutionChildren(commandContext, initialFlowElement, processInstance);

        return processInstance;
    }

    /**
     * Returns an active (not suspended) process given a process definition.
     *
     * @param processDefinition the process definition
     * @return the active process by the definition.
     */
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

    /**
     * Returns the initial element of the flow of the process.
     *
     * @param process the process to inspect for the initial element
     * @param processDefinitionId the process definitionId to use when reporting an error
     * @return the initial flow element
     */
    public FlowElement getInitialFlowElement(Process process, String processDefinitionId) {
        FlowElement initialFlowElement = process.getInitialFlowElement();

        if (initialFlowElement == null) {
            throw new ActivitiException(noStartElementMessage(processDefinitionId));
        }

        return initialFlowElement;
    }

    /**
     * Create the first execution that will visit all the process definition elements
     *
     * @param commandContext the commandContext
     * @param initialFlowElement the initial element of the flow
     * @param processInstance the process instance
     */
    private void createProcessExecutionChildren(
        CommandContext commandContext,
        FlowElement initialFlowElement,
        ExecutionEntity processInstance) {

        ExecutionEntity execution = commandContext
            .getExecutionEntityManager()
            .createChildExecution(processInstance);

        execution.setCurrentFlowElement(initialFlowElement);
    }

    /**
     * Set the process name in the history of the process
     *
     * @param commandContext the command context
     * @param processInstanceName the process instance name to be set
     * @param processInstance the process instance
     */
    private void setProcessInstanceNameInHistory(
        CommandContext commandContext,
        String processInstanceName,
        ExecutionEntity processInstance) {

        if (processInstanceName == null) {
            return;
        }

        processInstance.setName(processInstanceName);

        commandContext.getHistoryManager()
            .recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
    }

    /**
     * Create a process instance in the database using the ExecutionEntityManager
     *
     * @param commandContext the command context
     * @param processDefinition the process definition
     * @param businessKey the business key
     * @param initialFlowElement the initial flow element
     * @return the ExecutionEntity
     */
    private ExecutionEntity createProcessInstanceFromExecutionEntityManager(
        CommandContext commandContext,
        ProcessDefinition processDefinition,
        String businessKey,
        FlowElement initialFlowElement) {

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

    public void startProcessInstance(
        CommandContext commandContext,
        ExecutionEntity processInstance,
        Process process,
        Map<String, Object> variables,
        Map<String, Object> transientVariables,
        FlowElement initialFlowElement) {

        createProcessVariables(processInstance, variables, transientVariables, process.getDataObjects());
        recordStartProcessInstance(commandContext, initialFlowElement, processInstance);

        List<MessageEventSubscriptionEntity> messageEventSubscriptions = new ArrayList<>();

        // Event sub process handling
        for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof EventSubProcess) {
                EventSubProcess eventSubProcess = (EventSubProcess) flowElement;
                for (FlowElement subElement : eventSubProcess.getFlowElements()) {
                    if (subElement instanceof StartEvent) {
                        StartEvent startEvent = (StartEvent) subElement;
                        if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions())) {
                            EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);

                            if (eventDefinition instanceof MessageEventDefinition) {
                                MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition;

                                BpmnModel bpmnModel = getBpmnModel(processInstance.getProcessDefinitionId());
                                if (bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
                                    messageEventDefinition.setMessageRef(bpmnModel.getMessage(messageEventDefinition.getMessageRef()).getName());
                                }

                                ExecutionEntity messageExecution = commandContext.getExecutionEntityManager().createChildExecution(processInstance);
                                messageExecution.setCurrentFlowElement(startEvent);
                                messageExecution.setEventScope(true);

                                String messageName = getMessageName(commandContext, messageEventDefinition, messageExecution);

                                MessageEventSubscriptionEntity subscription =
                                    commandContext.getEventSubscriptionEntityManager()
                                        .insertMessageEvent(messageName, messageExecution);

                                Optional<String> correlationKey = getCorrelationKey(commandContext, messageEventDefinition, messageExecution);
                                correlationKey.ifPresent(subscription::setConfiguration);

                                messageEventSubscriptions.add(subscription);
                            }
                        }
                    }
                }
            }
        }

        ExecutionEntity execution = processInstance.getExecutions().get(0); // There will always be one child execution created
        execution.setAppVersion(processInstance.getAppVersion());
        commandContext.getAgenda().planContinueProcessOperation(execution);

        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
            eventDispatcher.dispatchEvent(
                ActivitiEventBuilder.createProcessStartedEvent(execution, variables, false));

            for (MessageEventSubscriptionEntity messageEventSubscription : messageEventSubscriptions) {
                commandContext.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createMessageWaitingEvent(messageEventSubscription.getExecution(),
                        messageEventSubscription.getEventName(),
                        messageEventSubscription.getConfiguration()));
            }
        }
    }


    private void createProcessVariables(
        ExecutionEntity processInstance,
        Map<String, Object> variables,
        Map<String, Object> transientVariables,
        List<ValuedDataObject> dataObjects) {

        processInstance.setVariables(processDataObjects(dataObjects));

        // Set the variables passed into the start command
        if (variables != null) {
            variables.forEach(processInstance::setVariable);
        }

        if (transientVariables != null) {
            transientVariables.forEach(processInstance::setVariable);
        }

        // Fire events
        ProcessEngineConfigurationImpl conf = Context.getProcessEngineConfiguration();

        if (conf.getEventDispatcher().isEnabled()) {
            conf.getEventDispatcher()
                .dispatchEvent(
                    createEntityWithVariablesEvent(
                        ENTITY_INITIALIZED,
                        processInstance,
                        variables,
                        false));
        }
    }

    /**
     * Convert data objects to process variables
     *
     * @param dataObjects collection of dataObjects
     * @return a map of values
     */
    protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
        if (dataObjects == null) {
            return new HashMap<>();
        }

        return dataObjects.stream().collect(toMap(ValuedDataObject::getName, ValuedDataObject::getValue));
    }

    protected String getMessageName(CommandContext commandContext,
        MessageEventDefinition messageEventDefinition,
        DelegateExecution execution) {
        ExpressionManager expressionManager = commandContext.getProcessEngineConfiguration()
            .getExpressionManager();


        String messageName = Optional.ofNullable(messageEventDefinition.getMessageRef())
            .orElse(messageEventDefinition.getMessageExpression());

        Expression expression = expressionManager.createExpression(messageName);

        return expression.getValue(execution)
            .toString();
    }

    protected Optional<String> getCorrelationKey(CommandContext commandContext,
        MessageEventDefinition messageEventDefinition,
        DelegateExecution execution) {
        ExpressionManager expressionManager = commandContext.getProcessEngineConfiguration()
            .getExpressionManager();

        return Optional.ofNullable(messageEventDefinition.getCorrelationKey())
            .map(correlationKey -> {
                Expression expression = expressionManager.createExpression(messageEventDefinition.getCorrelationKey());

                return expression.getValue(execution)
                    .toString();
            });
    }

    private void recordStartProcessInstance(CommandContext commandContext, FlowElement initialFlowElement, ExecutionEntity processInstance){
        updateProcessInstanceStartDate(processInstance);
        commandContext.getHistoryManager().recordProcessInstanceStart(processInstance, initialFlowElement);
    }

    private void updateProcessInstanceStartDate(ExecutionEntity processInstance) {
        CommandContext commandContext = Context.getCommandContext();
        commandContext.getExecutionEntityManager().updateProcessInstanceStartDate(processInstance);
    }

    // Error messages for the exceptions
    private String processSuspendedMessage(ProcessDefinition processDefinition) {
        return "Cannot start process instance. Process definition " + processDefinition.getName()
            + " (id = " + processDefinition.getId() + ") is suspended";
    }

    private String processNotFoundMessage(ProcessDefinition processDefinition) {
        return "Cannot start process instance. Process model " + processDefinition.getName()
            + " (id = " + processDefinition.getId() + ") could not be found";
    }

    private String noStartElementMessage(String processDefinitionId) {
        return "No start element found for process definition " + processDefinitionId;
    }


}

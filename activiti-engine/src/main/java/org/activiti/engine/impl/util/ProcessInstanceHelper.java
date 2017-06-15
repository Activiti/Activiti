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
package org.activiti.engine.impl.util;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class ProcessInstanceHelper {

  public ProcessInstance createProcessInstance(ProcessDefinitionEntity processDefinition,
      String businessKey, String processInstanceName, Map<String, Object> variables, Map<String, Object> transientVariables) {

    return createAndStartProcessInstance(processDefinition, businessKey, processInstanceName, variables, transientVariables, false);
  }

  public ProcessInstance createAndStartProcessInstance(ProcessDefinition processDefinition,
      String businessKey, String processInstanceName, Map<String, Object> variables, Map<String, Object> transientVariables) {

    return createAndStartProcessInstance(processDefinition, businessKey, processInstanceName, variables, transientVariables, true);
  }

  protected ProcessInstance createAndStartProcessInstance(ProcessDefinition processDefinition,
      String businessKey, String processInstanceName,
      Map<String, Object> variables, Map<String, Object> transientVariables, boolean startProcessInstance) {

    CommandContext commandContext = Context.getCommandContext(); // Todo: ideally, context should be passed here
    if (Activiti5Util.isActiviti5ProcessDefinition(commandContext, processDefinition)) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler();
      return activiti5CompatibilityHandler.startProcessInstance(processDefinition.getKey(), processDefinition.getId(),
          variables, businessKey, processDefinition.getTenantId(), processInstanceName);
    }

    // Do not start process a process instance if the process definition is suspended
    if (ProcessDefinitionUtil.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiException("Cannot start process instance. Process definition " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }

    // Get model from cache
    Process process = ProcessDefinitionUtil.getProcess(processDefinition.getId());
    if (process == null) {
      throw new ActivitiException("Cannot start process instance. Process model " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") could not be found");
    }

    FlowElement initialFlowElement = process.getInitialFlowElement();
    if (initialFlowElement == null) {
      throw new ActivitiException("No start element found for process definition " + processDefinition.getId());
    }

    return createAndStartProcessInstanceWithInitialFlowElement(processDefinition, businessKey,
        processInstanceName, initialFlowElement, process, variables, transientVariables, startProcessInstance);
  }

  public ProcessInstance createAndStartProcessInstanceByMessage(ProcessDefinition processDefinition, String messageName,
      Map<String, Object> variables, Map<String, Object> transientVariables) {

    CommandContext commandContext = Context.getCommandContext();
    if (processDefinition.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
        Activiti5CompatibilityHandler activiti5CompatibilityHandler = commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler();

        if (activiti5CompatibilityHandler == null) {
          throw new ActivitiException("Found Activiti 5 process definition, but no compatibility handler on the classpath");
        }

        return activiti5CompatibilityHandler.startProcessInstanceByMessage(messageName, variables, null, processDefinition.getTenantId());

      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinition.getId() + " : " + processDefinition.getEngineVersion());
      }
    }

    // Do not start process a process instance if the process definition is suspended
    if (ProcessDefinitionUtil.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiException("Cannot start process instance. Process definition " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }

    // Get model from cache
    Process process = ProcessDefinitionUtil.getProcess(processDefinition.getId());
    if (process == null) {
      throw new ActivitiException("Cannot start process instance. Process model " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") could not be found");
    }

    FlowElement initialFlowElement = null;
    for (FlowElement flowElement : process.getFlowElements()) {
      if (flowElement instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) flowElement;
        if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions()) && startEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {

          MessageEventDefinition messageEventDefinition = (MessageEventDefinition) startEvent.getEventDefinitions().get(0);
          if (messageEventDefinition.getMessageRef().equals(messageName)) {
            initialFlowElement = flowElement;
            break;
          }
        }
      }
    }
    if (initialFlowElement == null) {
      throw new ActivitiException("No message start event found for process definition " + processDefinition.getId() + " and message name " + messageName);
    }

    return createAndStartProcessInstanceWithInitialFlowElement(processDefinition, null, null, initialFlowElement, process, variables, transientVariables, true);
  }

  public ProcessInstance createAndStartProcessInstanceWithInitialFlowElement(ProcessDefinition processDefinition,
      String businessKey, String processInstanceName, FlowElement initialFlowElement,
      Process process, Map<String, Object> variables, Map<String, Object> transientVariables, boolean startProcessInstance) {

    CommandContext commandContext = Context.getCommandContext();

    // Create the process instance
    String initiatorVariableName = null;
    if (initialFlowElement instanceof StartEvent) {
      initiatorVariableName = ((StartEvent) initialFlowElement).getInitiator();
    }

    ExecutionEntity processInstance = commandContext.getExecutionEntityManager()
    		.createProcessInstanceExecution(processDefinition, businessKey, processDefinition.getTenantId(), initiatorVariableName);

    commandContext.getHistoryManager().recordProcessInstanceStart(processInstance, initialFlowElement);

    processInstance.setVariables(processDataObjects(process.getDataObjects()));

    // Set the variables passed into the start command
    if (variables != null) {
      for (String varName : variables.keySet()) {
        processInstance.setVariable(varName, variables.get(varName));
      }
    }
    if (transientVariables != null) {
      for (String varName : transientVariables.keySet()) {
        processInstance.setTransientVariable(varName, transientVariables.get(varName));
      }
    }

    // Set processInstance name
    if (processInstanceName != null) {
      processInstance.setName(processInstanceName);
      commandContext.getHistoryManager().recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
    }

    // Fire events
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher()
        .dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.ENTITY_INITIALIZED, processInstance, variables, false));
    }

    // Create the first execution that will visit all the process definition elements
    ExecutionEntity execution = commandContext.getExecutionEntityManager().createChildExecution(processInstance);
    execution.setCurrentFlowElement(initialFlowElement);
    
    if (startProcessInstance) {
        startProcessInstance(processInstance, commandContext, variables);
      }

      return processInstance;
    }

    public void startProcessInstance(ExecutionEntity processInstance, CommandContext commandContext, Map<String, Object> variables) {

      Process process = ProcessDefinitionUtil.getProcess(processInstance.getProcessDefinitionId());


    // Event sub process handling
      List<MessageEventSubscriptionEntity> messageEventSubscriptions = new LinkedList<>();
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
                BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processInstance.getProcessDefinitionId());
                if (bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
                  messageEventDefinition.setMessageRef(bpmnModel.getMessage(messageEventDefinition.getMessageRef()).getName());
                }
                ExecutionEntity messageExecution = commandContext.getExecutionEntityManager().createChildExecution(processInstance);
                messageExecution.setCurrentFlowElement(startEvent);
                messageExecution.setEventScope(true);
                messageEventSubscriptions
                .add(commandContext.getEventSubscriptionEntityManager().insertMessageEvent(messageEventDefinition.getMessageRef(), messageExecution));
              }
            }
          }
        }
      }
    }
    
    ExecutionEntity execution = processInstance.getExecutions().get(0); // There will always be one child execution created
    commandContext.getAgenda().planContinueProcessOperation(execution);

    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createProcessStartedEvent(execution, variables, false));
        
        for (MessageEventSubscriptionEntity messageEventSubscription : messageEventSubscriptions) {
            commandContext.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createMessageEvent(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, messageEventSubscription.getActivityId(),
                            messageEventSubscription.getEventName(), null, messageEventSubscription.getExecution().getId(),
                            messageEventSubscription.getProcessInstanceId(), messageEventSubscription.getProcessDefinitionId()));
          }
    }
  }

  protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    // convert data objects to process variables
    if (dataObjects != null) {
      for (ValuedDataObject dataObject : dataObjects) {
        variablesMap.put(dataObject.getName(), dataObject.getValue());
      }
    }
    return variablesMap;
  }
}

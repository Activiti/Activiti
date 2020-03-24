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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

/**


 */
public class ProcessInstanceHelper {

    public ProcessInstance createAndStartProcessInstance(ProcessDefinition processDefinition,
                                                         String businessKey, String processInstanceName,
                                                         Map<String, Object> variables, Map<String, Object> transientVariables) {

    return createAndStartProcessInstance(processDefinition, businessKey, processInstanceName, variables, transientVariables, true);
}

    public Process getActiveProcess(ProcessDefinition processDefinition) {
        if (ProcessDefinitionUtil.isProcessDefinitionSuspended(processDefinition.getId())) {
            throw new ActivitiException("Cannot start process instance. Process definition " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
        }

        Process process = ProcessDefinitionUtil.getProcess(processDefinition.getId());
        if (process == null) {
            throw new ActivitiException("Cannot start process instance. Process model " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") could not be found");
        }
        return process;
    }

    public FlowElement getInitialFlowElement(Process process, String processDefinitionID) {
        FlowElement initialFlowElement = process.getInitialFlowElement();
        if (initialFlowElement == null) {
            throw new ActivitiException("No start element found for process definition " + processDefinitionID);
        }
        return initialFlowElement;
    }

  protected ProcessInstance createAndStartProcessInstance(ProcessDefinition processDefinition,
      String businessKey, String processInstanceName,
      Map<String, Object> variables, Map<String, Object> transientVariables, boolean startProcessInstance) {

      Process process = this.getActiveProcess(processDefinition);

      FlowElement initialFlowElement = this.getInitialFlowElement(process, processDefinition.getId());

      return createAndStartProcessInstanceWithInitialFlowElement(processDefinition, businessKey,
             processInstanceName, initialFlowElement, process, variables, transientVariables, startProcessInstance);
  }

    public ProcessInstance createProcessInstance(ProcessDefinition processDefinition, String businessKey,
                                                 String processInstanceName, Map<String, Object> variables,
                                                 Map<String, Object> transientVariables) {

        Process process = this.getActiveProcess(processDefinition);
        FlowElement initialFlowElement = this.getInitialFlowElement(process, processDefinition.getId());

        ExecutionEntity processInstance = createProcessInstanceWithInitialFlowElement(processDefinition,
                                                                                        businessKey,
                                                                                        processInstanceName,
                                                                                        initialFlowElement,
                                                                                        process,
                                                                                        variables,
                                                                                        transientVariables);
        return processInstance;
    }

  //TO DO
  public ProcessInstance createAndStartProcessInstanceByMessage(ProcessDefinition processDefinition, String businessKey, String messageName,
      Map<String, Object> messageVariables, Map<String, Object> transientVariables) {

      Process process = this.getActiveProcess(processDefinition);

      FlowElement initialFlowElement = null;
      BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinition.getId());
      for (FlowElement flowElement : process.getFlowElements()) {
          if (flowElement instanceof StartEvent) {
              StartEvent startEvent = (StartEvent) flowElement;
              if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions()) && startEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {

                  MessageEventDefinition messageEventDefinition = (MessageEventDefinition) startEvent.getEventDefinitions().get(0);
                  String messageRef = messageEventDefinition.getMessageRef();
                  if (messageRef.equals(messageName)) {
                      initialFlowElement = flowElement;
                      break;
                  } // FIXME: We should not need to reset eventDefinition messageRef to message name
                  else if (bpmnModel.containsMessageId(messageRef)) {
                      Message message = bpmnModel.getMessage(messageRef);
                      messageEventDefinition.setMessageRef(message.getName());
                      initialFlowElement = flowElement;
                      break;
                  }
              }
          }
      }
      if (initialFlowElement == null) {
          throw new ActivitiException("No message start event found for process definition " + processDefinition.getId() + " and message name " + messageName);
      }

      Map<String, Object> processVariables = messageVariables;

      // Create process instance with executions but defer to start process after dispatching ACTIVITY_MESSAGE_RECEIVED
      ExecutionEntity processInstance = createProcessInstanceWithInitialFlowElement(processDefinition,
          businessKey,
          null,
          initialFlowElement,
          process,
          processVariables,
          transientVariables);

      // Dispatch message received event
      dispatchStartMessageReceivedEvent(processInstance, messageName, messageVariables);

      // Finally start the process
      CommandContext commandContext = Context.getCommandContext();
      startProcessInstance(processInstance, commandContext, processVariables);

      return processInstance;
  }

  //TO DO
  public ProcessInstance createAndStartProcessInstanceWithInitialFlowElement(ProcessDefinition processDefinition,
      String businessKey, String processInstanceName, FlowElement initialFlowElement,
      Process process, Map<String, Object> variables, Map<String, Object> transientVariables, boolean startProcessInstance) {

        ExecutionEntity processInstance = createProcessInstanceWithInitialFlowElement(processDefinition,
                                                                                      businessKey,
                                                                                      processInstanceName,
                                                                                      initialFlowElement,
                                                                                      process,
                                                                                      variables,
                                                                                      transientVariables);
    if (startProcessInstance) {
        CommandContext commandContext = Context.getCommandContext();

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

                                String messageName = getMessageName(commandContext,
                                    messageEventDefinition,
                                    messageExecution);

                                MessageEventSubscriptionEntity subscription = commandContext.getEventSubscriptionEntityManager()
                                    .insertMessageEvent(messageName,
                                        messageExecution);
                                Optional<String> correlationKey = getCorrelationKey(commandContext,
                                    messageEventDefinition,
                                    messageExecution);
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
            eventDispatcher.dispatchEvent(ActivitiEventBuilder.createProcessStartedEvent(execution, variables, false));

            for (MessageEventSubscriptionEntity messageEventSubscription : messageEventSubscriptions) {
                commandContext.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createMessageWaitingEvent(messageEventSubscription.getExecution(),
                        messageEventSubscription.getEventName(),
                        messageEventSubscription.getConfiguration()));
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


    public ExecutionEntity createProcessInstanceWithInitialFlowElement(ProcessDefinition processDefinition,
                                                                       String businessKey,
                                                                       String processInstanceName,
                                                                       FlowElement initialFlowElement,
                                                                       Process process,
                                                                       Map<String, Object> variables,
                                                                       Map<String, Object> transientVariables) {
        CommandContext commandContext = Context.getCommandContext();

        // Create the process instance
        String initiatorVariableName = null;
        if (initialFlowElement instanceof StartEvent) {
            initiatorVariableName = ((StartEvent) initialFlowElement).getInitiator();
        }

        ExecutionEntity processInstance = commandContext.getExecutionEntityManager()
            .createProcessInstanceExecution(processDefinition,
                businessKey,
                processDefinition.getTenantId(),
                initiatorVariableName);

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
            commandContext.getHistoryManager()
                .recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
        }

        // Fire events
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration()
                .getEventDispatcher()
                .dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.ENTITY_INITIALIZED,
                    processInstance,
                    variables,
                    false));
        }

        // Create the first execution that will visit all the process definition elements
        ExecutionEntity execution = commandContext.getExecutionEntityManager().createChildExecution(processInstance);
        execution.setCurrentFlowElement(initialFlowElement);

        return processInstance;
    }

    protected void dispatchStartMessageReceivedEvent(ExecutionEntity processInstance,
                                                   String messageName,
                                                   Map<String, Object> variables) {
        // Dispatch message received event
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            // There will always be one child execution created
            DelegateExecution execution = processInstance.getExecutions().get(0);
            ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration()
                                                             .getEventDispatcher();
            eventDispatcher.dispatchEvent(ActivitiEventBuilder.createMessageReceivedEvent(execution,
                                                                                          messageName,
                                                                                          null,
                                                                                          variables));
        }
    }

}

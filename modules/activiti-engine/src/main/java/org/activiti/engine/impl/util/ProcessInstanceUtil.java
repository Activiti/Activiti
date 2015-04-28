package org.activiti.engine.impl.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.CollectionUtils;

public class ProcessInstanceUtil {

  public static ProcessInstance createAndStartProcessInstance(ProcessDefinitionEntity processDefinition, String businessKey, String processInstanceName, Map<String, Object> variables) {
    CommandContext commandContext = Context.getCommandContext();
    if (processDefinition.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
        Activiti5CompatibilityHandler activiti5CompatibilityHandler = commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler();

        if (activiti5CompatibilityHandler == null) {
          throw new ActivitiException("Found Activiti 5 process definition, but no compatibility handler on the classpath");
        }

        return activiti5CompatibilityHandler.startProcessInstance(processDefinition.getKey(), processDefinition.getId(), variables, businessKey, processDefinition.getTenantId(), processInstanceName);
      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinition.getId() + " : " + processDefinition.getEngineVersion());
      }
    }

    // Do not start process a process instance if the process definition is suspended
    if (processDefinition.isSuspended()) {
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

    return createAndStartProcessInstanceWithInitialFlowElement(processDefinition, businessKey, processInstanceName, initialFlowElement, process, variables);
  }

  public static ProcessInstance createAndStartProcessInstanceByMessage(ProcessDefinitionEntity processDefinition, String messageName, Map<String, Object> variables) {
    CommandContext commandContext = Context.getCommandContext();
    if (processDefinition.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
        Activiti5CompatibilityHandler activiti5CompatibilityHandler = commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler();

        if (activiti5CompatibilityHandler == null) {
          throw new ActivitiException("Found Activiti 5 process definition, but no compatibility handler on the classpath");
        }

        return activiti5CompatibilityHandler.startProcessInstance(processDefinition.getKey(), processDefinition.getId(), variables, null, processDefinition.getTenantId(), null);
      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinition.getId() + " : " + processDefinition.getEngineVersion());
      }
    }

    // Do not start process a process instance if the process definition is suspended
    if (processDefinition.isSuspended()) {
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
        if (CollectionUtils.isNotEmpty(startEvent.getEventDefinitions()) && startEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {

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

    return createAndStartProcessInstanceWithInitialFlowElement(processDefinition, null, null, initialFlowElement, process, variables);
  }

  public static ProcessInstance createAndStartProcessInstanceWithInitialFlowElement(ProcessDefinitionEntity processDefinition, String businessKey, String processInstanceName,
      FlowElement initialFlowElement, Process process, Map<String, Object> variables) {

    CommandContext commandContext = Context.getCommandContext();

    // Create the process instance
    String initiatorVariableName = null;
    if (initialFlowElement instanceof StartEvent) {
      initiatorVariableName = ((StartEvent) initialFlowElement).getInitiator();
    }

    ExecutionEntity processInstance = commandContext.getExecutionEntityManager().createProcessInstanceExecution(processDefinition.getId(), businessKey, processDefinition.getTenantId(),
        initiatorVariableName);
    commandContext.getHistoryManager().recordProcessInstanceStart(processInstance, initialFlowElement);

    processInstance.setVariables(processDataObjects(process.getDataObjects()));

    // Set the variables passed into the start command
    if (variables != null) {
      for (String varName : variables.keySet()) {
        processInstance.setVariable(varName, variables.get(varName));
      }
    }

    // Set processInstance name
    if (processInstanceName != null) {
      processInstance.setName(processInstanceName);
      commandContext.getHistoryManager().recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
    }

    // Create the first execution that will visit all the process definition elements
    ExecutionEntity execution = processInstance.createExecution();
    execution.setCurrentFlowElement(initialFlowElement);
    commandContext.getAgenda().planContinueProcessOperation(execution);

    return processInstance;
  }

  protected static Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
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

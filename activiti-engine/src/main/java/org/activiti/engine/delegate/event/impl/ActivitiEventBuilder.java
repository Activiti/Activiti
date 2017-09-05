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
package org.activiti.engine.delegate.event.impl;

import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiExceptionEvent;
import org.activiti.engine.delegate.event.ActivitiMembershipEvent;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.delegate.event.ActivitiSequenceFlowTakenEvent;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;

/**
 * Builder class used to create {@link ActivitiEvent} implementations.
 *

 */
public class ActivitiEventBuilder {

  /**
   * @param type
   *          type of event
   * @return an {@link ActivitiEvent} that doesn't have it's execution context-fields filled, as the event is a global event, independent of any running execution.
   */
  public static ActivitiEvent createGlobalEvent(ActivitiEventType type) {
    ActivitiEventImpl newEvent = new ActivitiEventImpl(type);
    return newEvent;
  }

  public static ActivitiEvent createEvent(ActivitiEventType type, String executionId, String processInstanceId, String processDefinitionId) {
    ActivitiEventImpl newEvent = new ActivitiEventImpl(type);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    return newEvent;
  }

  /**
   * @param type
   *          type of event
   * @param entity
   *          the entity this event targets
   * @return an {@link ActivitiEntityEvent}. In case an {@link ExecutionContext} is active, the execution related event fields will be populated. If not, execution details will be retrieved from the
   *         {@link Object} if possible.
   */
  public static ActivitiEntityEvent createEntityEvent(ActivitiEventType type, Object entity) {
    ActivitiEntityEventImpl newEvent = new ActivitiEntityEventImpl(entity, type);

    // In case an execution-context is active, populate the event fields
    // related to the execution
    populateEventWithCurrentContext(newEvent);
    return newEvent;
  }

  /**
   * @param entity
   *            the entity this event targets
   * @param variables
   *            the variables associated with this entity
   * @return an {@link ActivitiEntityEvent}. In case an {@link ExecutionContext} is active, the execution related
   *         event fields will be populated. If not, execution details will be reteived from the {@link Object} if
   *         possible.
   */
  @SuppressWarnings("rawtypes")
  public static ActivitiProcessStartedEvent createProcessStartedEvent(final Object entity,
          final Map variables, final boolean localScope) {
      final ActivitiProcessStartedEventImpl newEvent = new ActivitiProcessStartedEventImpl(entity, variables, localScope);

      // In case an execution-context is active, populate the event fields related to the execution
      populateEventWithCurrentContext(newEvent);
      return newEvent;
  }

  /**
   * @param type
   *          type of event
   * @param entity
   *          the entity this event targets
   * @param variables
   *          the variables associated with this entity
   * @return an {@link ActivitiEntityEvent}. In case an {@link ExecutionContext} is active, the execution related event fields will be populated. If not, execution details will be retrieved from the
   *         {@link Object} if possible.
   */
  @SuppressWarnings("rawtypes")
  public static ActivitiEntityWithVariablesEvent createEntityWithVariablesEvent(ActivitiEventType type, Object entity, Map variables, boolean localScope) {
    ActivitiEntityWithVariablesEventImpl newEvent = new ActivitiEntityWithVariablesEventImpl(entity, variables, localScope, type);

    // In case an execution-context is active, populate the event fields
    // related to the execution
    populateEventWithCurrentContext(newEvent);
    return newEvent;
  }

  public static ActivitiSequenceFlowTakenEvent createSequenceFlowTakenEvent(ExecutionEntity executionEntity, ActivitiEventType type,
      String sequenceFlowId, String sourceActivityId, String sourceActivityName, String sourceActivityType, Object sourceActivityBehavior,
      String targetActivityId, String targetActivityName, String targetActivityType, Object targetActivityBehavior) {

    ActivitiSequenceFlowTakenEventImpl newEvent = new ActivitiSequenceFlowTakenEventImpl(type);

    if (executionEntity != null) {
      newEvent.setExecutionId(executionEntity.getId());
      newEvent.setProcessInstanceId(executionEntity.getProcessInstanceId());
      newEvent.setProcessDefinitionId(executionEntity.getProcessDefinitionId());
    }

    newEvent.setId(sequenceFlowId);
    newEvent.setSourceActivityId(sourceActivityId);
    newEvent.setSourceActivityName(sourceActivityName);
    newEvent.setSourceActivityType(sourceActivityType);
    newEvent.setSourceActivityBehaviorClass(sourceActivityBehavior != null ? sourceActivityBehavior.getClass().getCanonicalName() : null);
    newEvent.setTargetActivityId(targetActivityId);
    newEvent.setTargetActivityName(targetActivityName);
    newEvent.setTargetActivityType(targetActivityType);
    newEvent.setTargetActivityBehaviorClass(targetActivityBehavior != null ? targetActivityBehavior.getClass().getCanonicalName() :  null);

    return newEvent;
  }

  /**
   * @param type
   *          type of event
   * @param entity
   *          the entity this event targets
   * @return an {@link ActivitiEntityEvent}
   */
  public static ActivitiEntityEvent createEntityEvent(ActivitiEventType type, Object entity, String executionId, String processInstanceId, String processDefinitionId) {
    ActivitiEntityEventImpl newEvent = new ActivitiEntityEventImpl(entity, type);

    newEvent.setExecutionId(executionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    return newEvent;
  }

  /**
   * @param type
   *          type of event
   * @param entity
   *          the entity this event targets
   * @param cause
   *          the cause of the event
   * @return an {@link ActivitiEntityEvent} that is also instance of {@link ActivitiExceptionEvent}. In case an {@link ExecutionContext} is active, the execution related event fields will be
   *         populated.
   */
  public static ActivitiEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause) {
    ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

    // In case an execution-context is active, populate the event fields
    // related to the execution
    populateEventWithCurrentContext(newEvent);
    return newEvent;
  }

  /**
   * @param type
   *          type of event
   * @param entity
   *          the entity this event targets
   * @param cause
   *          the cause of the event
   * @return an {@link ActivitiEntityEvent} that is also instance of {@link ActivitiExceptionEvent}.
   */
  public static ActivitiEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause, String executionId, String processInstanceId, String processDefinitionId) {
    ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

    newEvent.setExecutionId(executionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    return newEvent;
  }

  public static ActivitiActivityEvent createActivityEvent(ActivitiEventType type, String activityId, String activityName, String executionId,
      String processInstanceId, String processDefinitionId, FlowElement flowElement) {

    ActivitiActivityEventImpl newEvent = new ActivitiActivityEventImpl(type);
    newEvent.setActivityId(activityId);
    newEvent.setActivityName(activityName);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);

    if (flowElement instanceof FlowNode) {
      FlowNode flowNode = (FlowNode) flowElement;
      newEvent.setActivityType(parseActivityType(flowNode));
      Object behaviour = flowNode.getBehavior();
      if (behaviour != null) {
        newEvent.setBehaviorClass(behaviour.getClass().getCanonicalName());
      }
    }

    return newEvent;
  }

  protected static String parseActivityType(FlowNode flowNode) {
    String elementType = flowNode.getClass().getSimpleName();
    elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
    return elementType;
  }

  public static ActivitiActivityCancelledEvent createActivityCancelledEvent(String activityId, String activityName, String executionId, 
      String processInstanceId, String processDefinitionId, String activityType, Object cause) {

    ActivitiActivityCancelledEventImpl newEvent = new ActivitiActivityCancelledEventImpl();
    newEvent.setActivityId(activityId);
    newEvent.setActivityName(activityName);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setActivityType(activityType);
    newEvent.setCause(cause);
    return newEvent;
  }

  public static ActivitiCancelledEvent createCancelledEvent(String executionId, String processInstanceId, String processDefinitionId, Object cause) {
    ActivitiProcessCancelledEventImpl newEvent = new ActivitiProcessCancelledEventImpl();
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setCause(cause);
    return newEvent;
  }

  public static ActivitiSignalEvent createSignalEvent(ActivitiEventType type, String activityId, String signalName, Object signalData, String executionId, String processInstanceId,
      String processDefinitionId) {
    ActivitiSignalEventImpl newEvent = new ActivitiSignalEventImpl(type);
    newEvent.setActivityId(activityId);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setSignalName(signalName);
    newEvent.setSignalData(signalData);
    return newEvent;
  }

  public static ActivitiMessageEvent createMessageEvent(ActivitiEventType type, String activityId, String messageName, Object payload, String executionId, String processInstanceId,
      String processDefinitionId) {
    ActivitiMessageEventImpl newEvent = new ActivitiMessageEventImpl(type);
    newEvent.setActivityId(activityId);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setMessageName(messageName);
    newEvent.setMessageData(payload);
    return newEvent;
  }

  public static ActivitiErrorEvent createErrorEvent(ActivitiEventType type, String activityId, String errorId, String errorCode,
      String executionId, String processInstanceId, String processDefinitionId) {
    ActivitiErrorEventImpl newEvent = new ActivitiErrorEventImpl(type);
    newEvent.setActivityId(activityId);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    newEvent.setErrorId(errorId);
    newEvent.setErrorCode(errorCode);
    return newEvent;
  }

  public static ActivitiVariableEvent createVariableEvent(ActivitiEventType type, String variableName, Object variableValue, VariableType variableType, String taskId, String executionId,
      String processInstanceId, String processDefinitionId) {
    ActivitiVariableEventImpl newEvent = new ActivitiVariableEventImpl(type);
    newEvent.setVariableName(variableName);
    newEvent.setVariableValue(variableValue);
    newEvent.setVariableType(variableType);
    newEvent.setTaskId(taskId);
    newEvent.setExecutionId(executionId);
    newEvent.setProcessDefinitionId(processDefinitionId);
    newEvent.setProcessInstanceId(processInstanceId);
    return newEvent;
  }

  public static ActivitiMembershipEvent createMembershipEvent(ActivitiEventType type, String groupId, String userId) {
    ActivitiMembershipEventImpl newEvent = new ActivitiMembershipEventImpl(type);
    newEvent.setUserId(userId);
    newEvent.setGroupId(groupId);
    return newEvent;
  }

  protected static void populateEventWithCurrentContext(ActivitiEventImpl event) {
    if (event instanceof ActivitiEntityEvent) {
      Object persistedObject = ((ActivitiEntityEvent) event).getEntity();
      if (persistedObject instanceof Job) {
        event.setExecutionId(((Job) persistedObject).getExecutionId());
        event.setProcessInstanceId(((Job) persistedObject).getProcessInstanceId());
        event.setProcessDefinitionId(((Job) persistedObject).getProcessDefinitionId());
      } else if (persistedObject instanceof DelegateExecution) {
        event.setExecutionId(((DelegateExecution) persistedObject).getId());
        event.setProcessInstanceId(((DelegateExecution) persistedObject).getProcessInstanceId());
        event.setProcessDefinitionId(((DelegateExecution) persistedObject).getProcessDefinitionId());
      } else if (persistedObject instanceof IdentityLinkEntity) {
        IdentityLinkEntity idLink = (IdentityLinkEntity) persistedObject;
        if (idLink.getProcessDefinitionId() != null) {
          event.setProcessDefinitionId(idLink.getProcessDefId());
        } else if (idLink.getProcessInstance() != null) {
          event.setProcessDefinitionId(idLink.getProcessInstance().getProcessDefinitionId());
          event.setProcessInstanceId(idLink.getProcessInstanceId());
          event.setExecutionId(idLink.getProcessInstanceId());
        } else if (idLink.getTask() != null) {
          event.setProcessDefinitionId(idLink.getTask().getProcessDefinitionId());
          event.setProcessInstanceId(idLink.getTask().getProcessInstanceId());
          event.setExecutionId(idLink.getTask().getExecutionId());
        }
      } else if (persistedObject instanceof Task) {
        event.setProcessInstanceId(((Task) persistedObject).getProcessInstanceId());
        event.setExecutionId(((Task) persistedObject).getExecutionId());
        event.setProcessDefinitionId(((Task) persistedObject).getProcessDefinitionId());
      } else if (persistedObject instanceof ProcessDefinition) {
        event.setProcessDefinitionId(((ProcessDefinition) persistedObject).getId());
      }
    }
  }
}

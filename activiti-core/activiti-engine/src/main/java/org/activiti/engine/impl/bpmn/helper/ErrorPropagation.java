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

package org.activiti.engine.impl.bpmn.helper;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Error;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * This class is responsible for finding and executing error handlers for BPMN Errors.
 *
 * Possible error handlers include Error Intermediate Events and Error Event Sub-Processes.
 *


 */
public class ErrorPropagation {

  public static void propagateError(BpmnError error, DelegateExecution execution) {
    propagateError(error.getErrorCode(), execution);
  }

  public static void propagateError(String errorRef, DelegateExecution execution) {
    Map<String, List<Event>> eventMap = findCatchingEventsForProcess(execution.getProcessDefinitionId(), errorRef);
    if (eventMap.size() > 0) {
      executeCatch(eventMap, execution, errorRef);
    } else if (!execution.getProcessInstanceId().equals(execution.getRootProcessInstanceId())) { // Call activity

      ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
      ExecutionEntity processInstanceExecution = executionEntityManager.findById(execution.getProcessInstanceId());
      if (processInstanceExecution != null) {

        ExecutionEntity parentExecution = processInstanceExecution.getSuperExecution();

        Set<String> toDeleteProcessInstanceIds = new HashSet<String>();
        toDeleteProcessInstanceIds.add(execution.getProcessInstanceId());

        while (parentExecution != null && eventMap.size() == 0) {
          eventMap = findCatchingEventsForProcess(parentExecution.getProcessDefinitionId(), errorRef);
          if (eventMap.size() > 0) {

            for (String processInstanceId : toDeleteProcessInstanceIds) {
              ExecutionEntity processInstanceEntity = executionEntityManager.findById(processInstanceId);

              // Delete
              executionEntityManager.deleteProcessInstanceExecutionEntity(processInstanceEntity.getId(),
                  execution.getCurrentFlowElement() != null ? execution.getCurrentFlowElement().getId() : null,
                  "ERROR_EVENT " + errorRef,
                  false, false);

              // Event
              if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
                Context.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT, processInstanceEntity));
              }
            }
            executeCatch(eventMap, parentExecution, errorRef);

          } else {
            toDeleteProcessInstanceIds.add(parentExecution.getProcessInstanceId());
            ExecutionEntity superExecution = parentExecution.getSuperExecution();
            if (superExecution != null) {
              parentExecution = superExecution;
            } else if (!parentExecution.getId().equals(parentExecution.getRootProcessInstanceId())) { // stop at the root
                parentExecution = parentExecution.getProcessInstance();
            } else {
              parentExecution = null;
            }
          }
        }

      }

    }

    if (eventMap.size() == 0) {
      throw new BpmnError(errorRef, "No catching boundary event found for error with errorCode '" + errorRef + "', neither in same process nor in parent process");
    }
  }

  protected static void executeCatch(Map<String, List<Event>> eventMap, DelegateExecution delegateExecution, String errorId) {
    Event matchingEvent = null;
    ExecutionEntity currentExecution = (ExecutionEntity) delegateExecution;
    ExecutionEntity parentExecution = null;

    if (eventMap.containsKey(currentExecution.getActivityId())) {
      matchingEvent = eventMap.get(currentExecution.getActivityId()).get(0);

      // Check for multi instance
      if (currentExecution.getParentId() != null && currentExecution.getParent().isMultiInstanceRoot()) {
        parentExecution = currentExecution.getParent();
      } else {
        parentExecution = currentExecution;
      }

    } else {
      parentExecution = currentExecution.getParent();

      // Traverse parents until one is found that is a scope and matches the activity the boundary event is defined on
      while (matchingEvent == null && parentExecution != null) {
        FlowElementsContainer currentContainer = null;
        if (parentExecution.getCurrentFlowElement() instanceof FlowElementsContainer) {
          currentContainer = (FlowElementsContainer) parentExecution.getCurrentFlowElement();
        } else if (parentExecution.getId().equals(parentExecution.getProcessInstanceId())) {
          currentContainer = ProcessDefinitionUtil.getProcess(parentExecution.getProcessDefinitionId());
        }

        for (String refId : eventMap.keySet()) {
          List<Event> events = eventMap.get(refId);
          if (CollectionUtil.isNotEmpty(events) && events.get(0) instanceof StartEvent) {
            if (currentContainer.getFlowElement(refId) != null) {
              matchingEvent = events.get(0);
            }
          }
        }

        if (matchingEvent == null) {
          if (eventMap.containsKey(parentExecution.getActivityId())) {
            matchingEvent = eventMap.get(parentExecution.getActivityId()).get(0);

            // Check for multi instance
            if (parentExecution.getParentId() != null && parentExecution.getParent().isMultiInstanceRoot()) {
              parentExecution = parentExecution.getParent();
            }

          } else if (StringUtils.isNotEmpty(parentExecution.getParentId())) {
            parentExecution = parentExecution.getParent();
          } else {
            parentExecution = null;
          }
        }
      }
    }

    if (matchingEvent != null && parentExecution != null) {
      executeEventHandler(matchingEvent, parentExecution, currentExecution, errorId);
    } else {
      throw new ActivitiException("No matching parent execution for error code " + errorId + " found");
    }
  }

  protected static void executeEventHandler(Event event, ExecutionEntity parentExecution, ExecutionEntity currentExecution, String errorId) {
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(parentExecution.getProcessDefinitionId());
      if (bpmnModel != null) {

        String errorCode = Optional.ofNullable(bpmnModel.getErrors().get(errorId))
                .map(Error::getErrorCode).orElse(errorId);

        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createErrorEvent(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, event.getId(), errorId, errorCode, parentExecution.getId(),
              parentExecution.getProcessInstanceId(), parentExecution.getProcessDefinitionId()));
      }
    }

    if (event instanceof StartEvent) {
      ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

      if (!currentExecution.getParentId().equals(parentExecution.getId())) {
        Context.getAgenda().planDestroyScopeOperation(currentExecution);
      } else {
        executionEntityManager.deleteExecutionAndRelatedData(currentExecution, null);
      }

      ExecutionEntity eventSubProcessExecution = executionEntityManager.createChildExecution(parentExecution);
      eventSubProcessExecution.setCurrentFlowElement(event);
      Context.getAgenda().planContinueProcessOperation(eventSubProcessExecution);

    } else {
      ExecutionEntity boundaryExecution = null;
      List<? extends ExecutionEntity> childExecutions = parentExecution.getExecutions();
      for (ExecutionEntity childExecution : childExecutions) {
        if (childExecution.getActivityId().equals(event.getId())) {
          boundaryExecution = childExecution;
        }
      }

      Context.getAgenda().planTriggerExecutionOperation(boundaryExecution);
    }
  }

  protected static Map<String, List<Event>> findCatchingEventsForProcess(String processDefinitionId, String errorRef) {
    Map<String, List<Event>> eventMap = new HashMap<String, List<Event>>();
    Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);

    String compareErrorCode = retrieveErrorCode(bpmnModel, errorRef);

    List<EventSubProcess> subProcesses = process.findFlowElementsOfType(EventSubProcess.class, true);
    for (EventSubProcess eventSubProcess : subProcesses) {
      for (FlowElement flowElement : eventSubProcess.getFlowElements()) {
        if (flowElement instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) flowElement;
          if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions()) && startEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
            ErrorEventDefinition errorEventDef = (ErrorEventDefinition) startEvent.getEventDefinitions().get(0);
            String eventErrorCode = retrieveErrorCode(bpmnModel, errorEventDef.getErrorRef());

            if (eventErrorCode == null || compareErrorCode == null || eventErrorCode.equals(compareErrorCode)) {
              List<Event> startEvents = new ArrayList<Event>();
              startEvents.add(startEvent);
              eventMap.put(eventSubProcess.getId(), startEvents);
            }
          }
        }
      }
    }

    List<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      if (boundaryEvent.getAttachedToRefId() != null && CollectionUtil.isNotEmpty(boundaryEvent.getEventDefinitions()) && boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {

        ErrorEventDefinition errorEventDef = (ErrorEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        String eventErrorCode = retrieveErrorCode(bpmnModel, errorEventDef.getErrorRef());

        if (eventErrorCode == null || compareErrorCode == null || eventErrorCode.equals(compareErrorCode)) {
          List<Event> elementBoundaryEvents = null;
          if (!eventMap.containsKey(boundaryEvent.getAttachedToRefId())) {
            elementBoundaryEvents = new ArrayList<Event>();
            eventMap.put(boundaryEvent.getAttachedToRefId(), elementBoundaryEvents);
          } else {
            elementBoundaryEvents = eventMap.get(boundaryEvent.getAttachedToRefId());
          }
          elementBoundaryEvents.add(boundaryEvent);
        }
      }
    }
    return eventMap;
  }

  public static boolean mapException(Exception e, ExecutionEntity execution, List<MapExceptionEntry> exceptionMap) {
    String errorCode = findMatchingExceptionMapping(e, exceptionMap);
    if (errorCode != null) {
      propagateError(errorCode, execution);
      return true;
    } else {
      ExecutionEntity callActivityExecution = null;
      ExecutionEntity parentExecution = execution.getParent();
      while (parentExecution != null && callActivityExecution == null) {
        if (parentExecution.getId().equals(parentExecution.getProcessInstanceId())) {
          if (parentExecution.getSuperExecution() != null) {
            callActivityExecution = parentExecution.getSuperExecution();
          } else {
            parentExecution = null;
          }
        } else {
          parentExecution = parentExecution.getParent();
        }
      }

      if (callActivityExecution != null) {
        CallActivity callActivity = (CallActivity) callActivityExecution.getCurrentFlowElement();
        if (CollectionUtil.isNotEmpty(callActivity.getMapExceptions())) {
          errorCode = findMatchingExceptionMapping(e, callActivity.getMapExceptions());
          if (errorCode != null) {
            propagateError(errorCode, callActivityExecution);
            return true;
          }
        }
      }

      return false;
    }
  }

  protected static String findMatchingExceptionMapping(Exception e, List<MapExceptionEntry> exceptionMap) {
    String defaultExceptionMapping = null;

    for (MapExceptionEntry me : exceptionMap) {
      String exceptionClass = me.getClassName();
      String errorCode = me.getErrorCode();

      // save the first mapping with no exception class as default map
      if (StringUtils.isNotEmpty(errorCode) && StringUtils.isEmpty(exceptionClass) && defaultExceptionMapping == null) {
        defaultExceptionMapping = errorCode;
        continue;
      }

      // ignore if error code or class are not defined
      if (StringUtils.isEmpty(errorCode) || StringUtils.isEmpty(exceptionClass)) {
        continue;
      }

      if (e.getClass().getName().equals(exceptionClass)) {
        return errorCode;
      }
      if (me.isAndChildren()) {
        Class<?> exceptionClassClass = ReflectUtil.loadClass(exceptionClass);
        if (exceptionClassClass.isAssignableFrom(e.getClass())) {
          return errorCode;
        }
      }
    }

    return defaultExceptionMapping;
  }

  protected static String retrieveErrorCode(BpmnModel bpmnModel, String errorRef) {
    return Optional.ofNullable(errorRef).map(ref -> {
      if(bpmnModel.containsErrorRef(errorRef)){
        return Optional.ofNullable(bpmnModel.getErrors().get(errorRef))
                .map(Error::getErrorCode).orElse(errorRef);
      }
      return errorRef;
    }).orElse(errorRef);
  }
}

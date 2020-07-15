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
 * @author Tijs Rademakers
 * @author Saeid Mirzaei
 */
public class ErrorPropagation {

  public static void propagateError(BpmnError error, DelegateExecution execution) {
    propagateError(error.getErrorCode(), execution);
  }

  public static void propagateError(String errorCode, DelegateExecution execution) {
    Map<String, List<Event>> eventMap = new HashMap<>();
   
    if (!execution.getProcessInstanceId().equals(execution.getRootProcessInstanceId())) { // Call activity
    	ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    	ExecutionEntity processInstanceExecution = executionEntityManager.findById(execution.getProcessInstanceId());
        if (processInstanceExecution != null) {
    	    ExecutionEntity parentExecution = (ExecutionEntity) execution;
            while (parentExecution.getParentId() != null || parentExecution.getSuperExecutionId() != null) {
               if (parentExecution.getParentId() != null) {
        	       parentExecution = parentExecution.getParent();
               } else {
                   parentExecution = parentExecution.getSuperExecution();
                   eventMap.putAll(findCatchingEventsForProcess(parentExecution.getProcessDefinitionId(), errorCode));  
               }
           }
        } 
     }
    
    eventMap.putAll(findCatchingEventsForProcess(execution.getProcessDefinitionId(), errorCode));
    
    if (eventMap.size() > 0) {
    	  
      executeCatch(eventMap, execution, errorCode);
    }

    if (eventMap.size() == 0) {
      throw new BpmnError(errorCode, "No catching boundary event found for error with errorCode '" + errorCode + "', neither in same process nor in parent process");
    }
  }

  protected static void executeCatch(Map<String, List<Event>> eventMap, DelegateExecution delegateExecution, String errorId) {
    Event matchingEvent = null;
    ExecutionEntity currentExecution = (ExecutionEntity) delegateExecution;
    ExecutionEntity parentExecution = null;
    Set<String> toDeleteProcessInstanceIds = new HashSet<String>();
    if (eventMap.containsKey(currentExecution.getActivityId())) {
    	
      // Check for multi instance
      if (currentExecution.getParentId() != null && currentExecution.getParent().isMultiInstanceRoot()) {
        parentExecution = currentExecution.getParent();
      } else {
        parentExecution = currentExecution;
      }
      
      matchingEvent = getMatchedCatchEventFromList(eventMap.get(currentExecution.getActivityId()), parentExecution, errorId);
      
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

        if(currentContainer!= null){
           for (String refId : eventMap.keySet()) {
                List<Event> events = eventMap.get(refId);
                if (CollectionUtil.isNotEmpty(events) && events.get(0) instanceof StartEvent) {
                    if (currentContainer.getFlowElement(refId) != null) {
                        matchingEvent = getMatchedCatchEventFromList(events, parentExecution, errorId);
                    }
                }
            }
        }

        if (matchingEvent == null) {
          if (eventMap.containsKey(parentExecution.getActivityId())) {
        	matchingEvent = getMatchedCatchEventFromList(eventMap.get(parentExecution.getActivityId()), parentExecution, errorId);

            // Check for multi instance
            if (parentExecution.getParentId() != null && parentExecution.getParent().isMultiInstanceRoot()) {
              parentExecution = parentExecution.getParent();
            }

            } else if (StringUtils.isNotEmpty(parentExecution.getParentId())) {
                 parentExecution = parentExecution.getParent();
            } else {
        	     if (!parentExecution.getProcessInstanceId().equals(parentExecution.getRootProcessInstanceId())) {
        		    toDeleteProcessInstanceIds.add(parentExecution.getProcessInstanceId());
                    parentExecution = parentExecution.getSuperExecution();
                  
                 }else {
                     parentExecution = null;
                 }
            }
         }
       }
     }

    if (matchingEvent != null && parentExecution != null) {
    	for (String processInstanceId : toDeleteProcessInstanceIds) {
    		ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
            ExecutionEntity processInstanceEntity = executionEntityManager.findById(processInstanceId);

            // Delete
            executionEntityManager.deleteProcessInstanceExecutionEntity(processInstanceEntity.getId(),
            	currentExecution.getCurrentFlowElement() != null ? currentExecution.getCurrentFlowElement().getId() : null,
                "ERROR_EVENT " + errorId, false, false);

            // Event
            if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
              Context.getProcessEngineConfiguration().getEventDispatcher()
                  .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT, processInstanceEntity));
            }
          }
      executeEventHandler(matchingEvent, parentExecution, currentExecution, errorId);
    } else {
      throw new ActivitiException("No matching parent execution for error code " + errorId + " found");
    }
  }

  protected static void executeEventHandler(Event event, ExecutionEntity parentExecution, ExecutionEntity currentExecution, String errorId) {
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(parentExecution.getProcessDefinitionId());
      if (bpmnModel != null) {

        String errorCode = bpmnModel.getErrors().get(errorId);
        if (errorCode == null) {
          errorCode = errorId;
        }

        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createErrorEvent(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, event.getId(), errorId, errorCode, parentExecution.getId(),
              parentExecution.getProcessInstanceId(), parentExecution.getProcessDefinitionId()));
      }
    }

    if (event instanceof StartEvent) {
      ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

      if (parentExecution.isProcessInstanceType()){
    	  executionEntityManager.deleteChildExecutions(parentExecution, null, true);
      } else if (currentExecution.getParentId().equals(parentExecution.getId()) == false) {
          Context.getAgenda().planDestroyScopeOperation(currentExecution);
      } else {
          executionEntityManager.deleteExecutionAndRelatedData(currentExecution, null, false);
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

  protected static Map<String, List<Event>> findCatchingEventsForProcess(String processDefinitionId, String errorCode) {
    Map<String, List<Event>> eventMap = new HashMap<String, List<Event>>();
    Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);

    String compareErrorCode = retrieveErrorCode(bpmnModel, errorCode);

    List<EventSubProcess> subProcesses = process.findFlowElementsOfType(EventSubProcess.class, true);
    for (EventSubProcess eventSubProcess : subProcesses) {
      for (FlowElement flowElement : eventSubProcess.getFlowElements()) {
        if (flowElement instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) flowElement;
          if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions()) && startEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
            ErrorEventDefinition errorEventDef = (ErrorEventDefinition) startEvent.getEventDefinitions().get(0);
            String eventErrorCode = retrieveErrorCode(bpmnModel, errorEventDef.getErrorCode());

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
      	  String eventErrorCode = retrieveErrorCode(bpmnModel, errorEventDef.getErrorCode());
      	  if (eventErrorCode == null || eventErrorCode.equals(compareErrorCode)) {
      		  List<Event> elementBoundaryEvents = null;
      		  if (eventMap.containsKey(boundaryEvent.getAttachedToRefId()) == false) {
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

  protected static String retrieveErrorCode(BpmnModel bpmnModel, String errorCode) {
    String finalErrorCode = null;
    if (errorCode != null && bpmnModel.containsErrorRef(errorCode)) {
      finalErrorCode = bpmnModel.getErrors().get(errorCode);
    } else {
      finalErrorCode = errorCode;
    }
    return finalErrorCode;
  }
  
  protected static Event getMatchedCatchEventFromList(List<Event> events, ExecutionEntity parentExecution, String errorId){
	Event matchedEvent = null;
    String matchedEventErrorCode = null;
    String errorCode = null;
	  
	BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(parentExecution.getProcessDefinitionId());  
	for(Event event: events){
		for (EventDefinition eventDefinition : event.getEventDefinitions()) {
			if (eventDefinition instanceof ErrorEventDefinition) {
				errorCode = ((ErrorEventDefinition) eventDefinition).getErrorCode();
			}
		}

		if (bpmnModel != null) {
			errorCode = retrieveErrorCode(bpmnModel, errorCode);
		}

		if(errorId == null && errorCode == null){
		   matchedEvent = event;
		   break;
		} else if (matchedEvent == null || (StringUtils.isNotEmpty(errorCode) && StringUtils.isEmpty(matchedEventErrorCode))) {
				  matchedEvent = event;
				  matchedEventErrorCode = errorCode;
		 	
		}
	}
	return matchedEvent;
  }
  
}
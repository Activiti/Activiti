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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.tree.ExecutionTree;
import org.activiti.engine.impl.util.tree.ExecutionTreeNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is responsible for finding and executing error handlers for BPMN Errors.
 * 
 * Possible error handlers include Error Intermediate Events and Error Event Sub-Processes.
 * 
 * @author Tijs Rademakers
 * @author Saeid Mirzaei
 */
public class ErrorPropagation {

  public static void propagateError(BpmnError error, ActivityExecution execution) {
    propagateError(error.getErrorCode(), execution);
  }

  public static void propagateError(String errorCode, ActivityExecution execution) {
    Map<String, List<Event>> eventMap = findCatchingEventsForProcess(execution.getProcessDefinitionId(), errorCode);
    if (eventMap.size() > 0) {
      executeCatch(eventMap, execution, errorCode);
    } else {
      if (execution.getProcessInstanceId().equals(execution.getRootProcessInstanceId()) == false) {
        ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
        ExecutionTree executionTree = executionEntityManager.findExecutionTree(execution.getRootProcessInstanceId());
        ExecutionTreeNode rootNode = executionTree.getRoot();
        
        // get execution node for current process instance
        ExecutionTreeNode existingNode = getCurrentExecutionTreeNode(rootNode, execution.getProcessInstanceId());
        if (existingNode != null) {
          
          // get execution node for parent process of current process instance
          ExecutionTreeNode parentNode = existingNode.getParent();
          
          List<ActivityExecution> toDeleteExecutions = new ArrayList<ActivityExecution>();
          toDeleteExecutions.add(execution);
          
          while (parentNode != null && eventMap.size() == 0) {
            eventMap = findCatchingEventsForProcess(parentNode.getExecutionEntity().getProcessDefinitionId(), errorCode);
            if (eventMap.size() > 0) {
              
              for (ActivityExecution toDeleteExecution : toDeleteExecutions) {
                executionEntityManager.deleteProcessInstanceExecutionEntity(toDeleteExecution.getProcessInstanceId(), 
                    toDeleteExecution.getCurrentFlowElement() != null ? toDeleteExecution.getCurrentFlowElement().getId() : null, "FINISHED");
              }
              executeCatch(eventMap, parentNode.getExecutionEntity(), errorCode);
            
            } else {
              toDeleteExecutions.add(parentNode.getExecutionEntity());
              parentNode = parentNode.getParent();
            }
          }
        }
      }
    }
    
    if (eventMap.size() == 0) {
      throw new BpmnError(errorCode, "No catching boundary event found for error with errorCode '" + errorCode + "', neither in same process nor in parent process");
    }
  }

  protected static void executeCatch(Map<String, List<Event>> eventMap, ActivityExecution activityExecution, String errorCode) {
    ExecutionEntity currentActivity = (ExecutionEntity) activityExecution;

    boolean matchingParentFound = false;

    /*
     * ScopeImpl catchingScope = errorHandler.getParent(); if (catchingScope instanceof ActivityImpl) { ActivityImpl catchingScopeActivity = (ActivityImpl) catchingScope; if
     * (!catchingScopeActivity.isScope()) { // event subprocesses catchingScope = catchingScopeActivity.getParent(); } }
     */

    // if (catchingScope instanceof PvmProcessDefinition) {
    // executeEventHandler(errorHandler, ((ExecutionEntity) execution).getProcessInstance(), errorCode);

    // } else {
    if (eventMap.containsKey(currentActivity.getActivityId())) {
      matchingParentFound = true;
    } else {
      CommandContext commandContext = Context.getCommandContext();
      currentActivity = commandContext.getExecutionEntityManager().findExecutionById(currentActivity.getParentId());

      // Traverse parents until one is found that is a scope and matches the activity the boundary event is defined on
      while (!matchingParentFound && currentActivity != null) {
        if (eventMap.containsKey(currentActivity.getActivityId())) {
          matchingParentFound = true;
        } else if (StringUtils.isNotEmpty(currentActivity.getParentId())) {
          currentActivity = commandContext.getExecutionEntityManager().findExecutionById(currentActivity.getParentId());
        } else {
          currentActivity = null;
        }
      }
    }

    if (matchingParentFound && currentActivity != null) {
      executeEventHandler(eventMap, currentActivity, errorCode);
    } else {
      throw new ActivitiException("No matching parent execution for error code " + errorCode + " found");
    }
    // }
  }

  protected static void executeEventHandler(Map<String, List<Event>> eventMap, ExecutionEntity boundaryParentExecution, String errorCode) {
    Event event = eventMap.get(boundaryParentExecution.getActivityId()).get(0);
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createErrorEvent(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, event.getId(), errorCode, boundaryParentExecution.getId(),
              boundaryParentExecution.getProcessInstanceId(), boundaryParentExecution.getProcessDefinitionId()));
    }

    if (event.getBehavior() instanceof EventSubProcessStartEventActivityBehavior) {
      boundaryParentExecution.setCurrentFlowElement(event.getSubProcess());
      boundaryParentExecution.performOperation(AtomicOperation.ACTIVITY_START); // make sure the listeners are invoked!
    } else {
      ExecutionEntity boundaryExecution = null;
      /*if (boundaryParentExecution.isScope() == false) {
        boundaryParentExecution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(boundaryParentExecution.getParentId());
      }*/
      List<ExecutionEntity> childExecutions = boundaryParentExecution.getExecutions();
      for (ExecutionEntity childExecution : childExecutions) {
        if (childExecution.getActivityId().equals(event.getId())) {
          boundaryExecution = childExecution;
        }
      }
      Context.getAgenda().planTriggerExecutionOperation(boundaryExecution);
    }
  }

  protected static Map<String, List<Event>> findCatchingEventsForProcess(String processDefinitionId, String errorCode) {
    Map<String, List<Event>> boundaryEventMap = new HashMap<String, List<Event>>();
    Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);

    String compareErrorCode = retrieveErrorCode(bpmnModel, errorCode);
    
    List<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      if (boundaryEvent.getAttachedToRefId() != null && CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) && boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {

        ErrorEventDefinition errorEventDef = (ErrorEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        String eventErrorCode = retrieveErrorCode(bpmnModel, errorEventDef.getErrorCode());
        
        if (eventErrorCode == null || compareErrorCode == null || eventErrorCode.equals(compareErrorCode)) {
          List<Event> elementBoundaryEvents = null;
          if (boundaryEventMap.containsKey(boundaryEvent.getAttachedToRefId()) == false) {
            elementBoundaryEvents = new ArrayList<Event>();
            boundaryEventMap.put(boundaryEvent.getAttachedToRefId(), elementBoundaryEvents);
          } else {
            elementBoundaryEvents = boundaryEventMap.get(boundaryEvent.getAttachedToRefId());
          }
          elementBoundaryEvents.add(boundaryEvent);
        }
      }
    }
    return boundaryEventMap;
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
        if (CollectionUtils.isNotEmpty(callActivity.getMapExceptions())) {
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
  
  protected static ExecutionTreeNode getCurrentExecutionTreeNode(ExecutionTreeNode treeNode, String processInstanceId) {
    if (treeNode.getChildren() != null && treeNode.getChildren().size() > 0) {
      ExecutionEntity childEntity = treeNode.getChildren().get(0).getExecutionEntity();
      if (childEntity != null && childEntity.getProcessInstanceId() != null && childEntity.getProcessInstanceId().equals(processInstanceId)) {
        return treeNode.getChildren().get(0);
      }
      
      for (ExecutionTreeNode childTreeNode : treeNode.getChildren()) {
        ExecutionTreeNode searchedNode = getCurrentExecutionTreeNode(childTreeNode, processInstanceId);
        if (searchedNode != null) {
          return searchedNode;
        }
      }
    }
    return null;
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
}

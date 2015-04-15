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
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.cache.ProcessDefinitionCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for finding and executing error handlers for BPMN
 * Errors.
 * 
 * Possible error handlers include Error Intermediate Events and Error Event
 * Sub-Processes.
 * 
 * @author Tijs Rademakers
 * @author Saeid Mirzaei
 */
public class ErrorPropagation {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorPropagation.class);

    public static void propagateError(BpmnError error, ActivityExecution execution) {
        propagateError(error.getErrorCode(), execution);
    }

    public static void propagateError(String errorCode, ActivityExecution execution) {

        while (execution != null) {
            Map<String, List<Event>> eventMap = findCatchingEventsForProcess(execution.getProcessDefinitionId(), errorCode);
            if (eventMap.size() > 0) {
                executeCatch(eventMap, execution, errorCode);
                break;
            }
            execution = getSuperExecution(execution);
        }
        if (execution == null) {
            throw new BpmnError(errorCode, "No catching boundary event found for error with errorCode '" + errorCode + "', neither in same process nor in parent process");
        }
    }

    protected static ActivityExecution getSuperExecution(ActivityExecution execution) {
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        ExecutionEntity superExecution = executionEntity.getProcessInstance().getSuperExecution();
        if (superExecution != null && !superExecution.isScope()) {
            return superExecution.getParent();
        }
        return superExecution;
    }

    protected static void executeCatch(Map<String, List<Event>> eventMap, ActivityExecution activityExecution, String errorCode) {
        ExecutionEntity currentActivity = ((ExecutionEntity) activityExecution);
        
        boolean matchingParentFound = false;
        
        /*ScopeImpl catchingScope = errorHandler.getParent();
        if (catchingScope instanceof ActivityImpl) {
            ActivityImpl catchingScopeActivity = (ActivityImpl) catchingScope;
            if (!catchingScopeActivity.isScope()) { // event subprocesses
                catchingScope = catchingScopeActivity.getParent();
            }
        }*/

        //if (catchingScope instanceof PvmProcessDefinition) {
        //    executeEventHandler(errorHandler, ((ExecutionEntity) execution).getProcessInstance(), errorCode);

        //} else {
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
        //}
    }

    protected static void executeEventHandler(Map<String, List<Event>> eventMap, ExecutionEntity execution, String errorCode) {
        Event event = eventMap.get(execution.getActivityId()).get(0);
        if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createErrorEvent(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, 
                            event.getId(), errorCode, execution.getId(), 
                            execution.getProcessInstanceId(), execution.getProcessDefinitionId()));
        }

        if (event.getBehavior() instanceof EventSubProcessStartEventActivityBehavior) {
            execution.setCurrentFlowElement(event.getSubProcess());
            execution.performOperation(AtomicOperation.ACTIVITY_START); // make sure the listeners are invoked!
        } else {
            execution.setCurrentFlowElement(event);
            Context.getAgenda().planContinueProcessOperation(execution);
        }
    }
    
    protected static Map<String, List<Event>> findCatchingEventsForProcess(String processDefinitionId, String errorCode) {
        Map<String, List<Event>> boundaryEventMap = new HashMap<String, List<Event>>();
        org.activiti.bpmn.model.Process process = ProcessDefinitionCacheUtil.getCachedProcess(processDefinitionId);

        List<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
        for (BoundaryEvent boundaryEvent : boundaryEvents) {
            if (boundaryEvent.getAttachedToRefId() != null && CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) &&
                    boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
                
                ErrorEventDefinition errorEventDef = (ErrorEventDefinition) boundaryEvent.getEventDefinitions().get(0);
                if (errorEventDef.getErrorCode() == null || errorCode == null || errorEventDef.getErrorCode().equals(errorCode)) {
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

    public static boolean mapException(Exception e, ActivityExecution execution, List<MapExceptionEntry> exceptionMap) {
        return mapException(e, execution, exceptionMap, false);
    }

    public static boolean mapException(Exception e, ActivityExecution execution, List<MapExceptionEntry> exceptionMap, boolean wrapped) {
        if (exceptionMap == null)
            return false;

        if (wrapped && e instanceof PvmException) {
            e = (Exception) ((PvmException) e).getCause();
        }

        String defaultMap = null;

        for (MapExceptionEntry me : exceptionMap) {
            String exceptionClass = me.getClassName();
            String errorCode = me.getErrorCode();

            // save the first mapping with no exception class as default map
            if (StringUtils.isNotEmpty(errorCode) && StringUtils.isEmpty(exceptionClass) && defaultMap == null) {
                defaultMap = errorCode;
                continue;
            }

            // ignore if error code or class are not defined
            if (StringUtils.isEmpty(errorCode) || StringUtils.isEmpty(exceptionClass))
                continue;

            if (e.getClass().getName().equals(exceptionClass)) {
                propagateError(errorCode, execution);
                return true;
            }
            if (me.isAndChildren()) {
                Class<?> exceptionClassClass = ReflectUtil.loadClass(exceptionClass);
                if (exceptionClassClass.isAssignableFrom(e.getClass())) {
                    propagateError(errorCode, execution);
                    return true;
                }
            }
        }
        if (defaultMap != null) {
            propagateError(defaultMap, execution);
            return true;
        }

        return false;
    }
}

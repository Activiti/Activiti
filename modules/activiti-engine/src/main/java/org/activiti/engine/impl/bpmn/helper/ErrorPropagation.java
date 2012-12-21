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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmScope;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for finding and executing error handlers for BPMN
 * Errors.
 * 
 * Possible error handlers include Error Intermediate Events and Error Event
 * Sub-Processes.
 * 
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class ErrorPropagation {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorPropagation.class);

  public static void propagateError(BpmnError error, ActivityExecution execution) throws Exception {    
    propagateError(error.getErrorCode(), execution);
  }
  
  public static void propagateError(String errorCode, ActivityExecution execution) throws Exception {
    // find local error handler
    String eventHandlerId = findLocalErrorEventHandler(execution, errorCode);  

    // TODO: merge two approaches (super process / regular process approach)
    if(eventHandlerId != null) {
      executeCatch(eventHandlerId, execution);
    }else {
      ActivityExecution superExecution = getSuperExecution(execution);
      if (superExecution != null) {
        executeCatchInSuperProcess(errorCode, superExecution);
      } else {
        LOG.info("{} throws error event with errorCode '{}', but no catching boundary event was defined. Execution will simply be ended (none end event semantics).",
                execution.getActivity().getId(), errorCode);
        execution.end();
      }
    }
  }

  private static String findLocalErrorEventHandler(ActivityExecution execution, String errorCode) {
    PvmScope scope = execution.getActivity();
    while (scope != null) {
      
      @SuppressWarnings("unchecked")
      List<ErrorEventDefinition> definitions = (List<ErrorEventDefinition>) scope.getProperty(BpmnParse.PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
      if(definitions != null) {      
        // definitions are sorted by precedence, ie. event subprocesses first.      
        for (ErrorEventDefinition errorEventDefinition : definitions) {
          if(errorEventDefinition.catches(errorCode)) {
            return scope.findActivity(errorEventDefinition.getHandlerActivityId()).getId();
          }
        }
      }
      
      // search for error handlers in parent scopes 
      if (scope instanceof PvmActivity) {
        scope = ((PvmActivity) scope).getParent();
      } else {
        scope = null;
      }
    }
    return null;
  }

  private static void executeCatchInSuperProcess(String errorCode, ActivityExecution superExecution) {
    String errorHandlerId = findLocalErrorEventHandler(superExecution, errorCode);
    if (errorHandlerId != null) {
      executeCatch(errorHandlerId, superExecution);
    } else { // no matching catch found, going one level up in process hierarchy
      ActivityExecution superSuperExecution = getSuperExecution(superExecution);
      if (superSuperExecution != null) {
        executeCatchInSuperProcess(errorCode, superSuperExecution);
      } else {
        throw new BpmnError(errorCode, "No catching boundary event found for error with errorCode '" 
                + errorCode + "', neither in same process nor in parent process");
      }
    }
  }
  
  private static ActivityExecution getSuperExecution(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ExecutionEntity superExecution = executionEntity.getProcessInstance().getSuperExecution();
    if (superExecution != null && !superExecution.isScope()) {
      return superExecution.getParent();
    }
    return superExecution;
  }
  
  private static void executeCatch(String errorHandlerId, ActivityExecution execution) {
    ProcessDefinitionImpl processDefinition = ((ExecutionEntity) execution).getProcessDefinition();
    ActivityImpl errorHandler = processDefinition.findActivity(errorHandlerId);
    if (errorHandler == null) {
      throw new ActivitiException(errorHandlerId + " not found in process definition");
    }
    
    boolean matchingParentFound = false;
    ActivityExecution leavingExecution = execution;
    ActivityImpl currentActivity = (ActivityImpl) execution.getActivity();    
    
    ScopeImpl catchingScope = errorHandler.getParent();
    if(catchingScope instanceof ActivityImpl) { 
      ActivityImpl catchingScopeActivity = (ActivityImpl) catchingScope;
      if(!catchingScopeActivity.isScope()) { // event subprocesses
        catchingScope = catchingScopeActivity.getParent();
      }
    }
    
    if(catchingScope instanceof PvmProcessDefinition) {
      executeEventHandler(errorHandler, ((ExecutionEntity)execution).getProcessInstance());
      
    } else {      
      if (currentActivity.getId().equals(catchingScope.getId())) {
        matchingParentFound = true;
      } else {
        currentActivity = (ActivityImpl) currentActivity.getParent();
      
        // Traverse parents until one is found that is a scope 
        // and matches the activity the boundary event is defined on
        while(!matchingParentFound && leavingExecution != null && currentActivity != null) {
          if (!leavingExecution.isConcurrent() && currentActivity.getId().equals(catchingScope.getId())) {
            matchingParentFound = true;
          } else if (leavingExecution.isConcurrent()) {
            leavingExecution = leavingExecution.getParent();
          } else {
            currentActivity = currentActivity.getParentActivity();
            leavingExecution = leavingExecution.getParent();
          } 
        }
        
        // Follow parents up until matching scope can't be found anymore (needed to support for multi-instance)
        while (leavingExecution != null
                && leavingExecution.getParent() != null 
                && leavingExecution.getParent().getActivity() != null
                && leavingExecution.getParent().getActivity().getId().equals(catchingScope.getId())) {
          leavingExecution = leavingExecution.getParent();
        }
      }
      
      if (matchingParentFound && leavingExecution != null) {
        executeEventHandler(errorHandler, leavingExecution);
      } else {
        throw new ActivitiException("No matching parent execution for activity " + errorHandlerId + " found");
      }
    }
        
  }

  private static void executeEventHandler(ActivityImpl borderEventActivity, ActivityExecution leavingExecution) {  
    if(borderEventActivity.getActivityBehavior() instanceof EventSubProcessStartEventActivityBehavior) {
      InterpretableExecution execution = (InterpretableExecution) leavingExecution;
      execution.setActivity(borderEventActivity.getParentActivity());
      execution.performOperation(AtomicOperation.ACTIVITY_START); // make sure the listeners are invoked!
    }else {
      leavingExecution.executeActivity(borderEventActivity);
    }
  }
  
}

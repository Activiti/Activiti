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

package org.activiti.engine.impl.bpmn.event;

import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmScope;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * This class is responsible for finding and executing error handlers for BPMN
 * Errors.
 * 
 * Possible error handlers include Error Intermediate Events and Error Event
 * Sub-Processes.
 * 
 * @author Falko Menge
 */
public class ErrorPropagation {

  protected static final Logger LOG = Logger.getLogger(ErrorPropagation.class.getName());

  public static void propagateError(BpmnError error, ActivityExecution execution) throws Exception {
    // find local error handler
    PvmActivity errorEventHandler = null;
    PvmScope scope = execution.getActivity();
    while (errorEventHandler == null && scope != null) {
      // search for error handler with same error code as thrown Error
      for (PvmActivity activity : scope.getActivities()) {
        if (((ActivityImpl) activity).getActivityBehavior() instanceof BoundaryEventActivityBehavior
                && error.getErrorCode().equals(activity.getProperty("errorCode"))) {
          errorEventHandler = activity;
          break;
        }
      }
      // search for generic error handler if no error handler with that error code has been found
      if (errorEventHandler == null) {
        for (PvmActivity activity : scope.getActivities()) {
          if (((ActivityImpl) activity).getActivityBehavior() instanceof BoundaryEventActivityBehavior
                  && (activity.getProperty("errorCode") == null || "".equals(activity.getProperty("errorCode")))) {
            errorEventHandler = activity;
            break;
          }
        }
        
      }
      // search for error handlers in parent scopes 
      if (errorEventHandler == null) {
        if (scope instanceof PvmActivity) {
          scope = ((PvmActivity) scope).getParent();
        } else {
          scope = null; // stop search
        }
      }
    }
    
    ErrorEndEventActivityBehavior errorEndEvent = new ErrorEndEventActivityBehavior(error.getErrorCode());
    if (errorEventHandler != null) {
      errorEndEvent.setBorderEventActivityId(errorEventHandler.getId());
    }
    // execute error handler
    errorEndEvent.execute(execution);
  }

  public static void propagateError(String errorCode, String eventHandlerId, ActivityExecution execution) {
    // TODO: merge two approaches (super process / regular process approach)
    
    // The borderEventActivityId is set during parsing (for performance reasons)
    // However, this only works on one process level (and not for call activities)
    // This is why there is a check for super processes  
    // in case the nested activity id has not been set
    
    if (eventHandlerId != null) {
      executeCatchInSameProcess(eventHandlerId, execution);
    } else { 
      ActivityExecution superExecution = getSuperExecution(execution);
      if (superExecution != null) {
        executeCatchInSuperProcess(errorCode, superExecution);
      } else {
        // TODO Shouldn't this be an exception as in executeCatchInSuperProcess() or is this dead code?
        LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
                + errorCode + "', but no catching boundary event was defined. "
                +   "Execution will simply be ended (none end event semantics).");
        execution.end();
      }
    }
  }

  protected static void executeCatchInSuperProcess(String errorCode, ActivityExecution superExecution) {
    ActivityExecution outgoingExecution = superExecution;
    ActivityImpl catchingActivity = (ActivityImpl) outgoingExecution.getActivity();
    boolean found = false;
    
    while (!found && outgoingExecution != null && catchingActivity != null) {
      for (ActivityImpl nestedActivity : catchingActivity.getActivities()) {
        if ("boundaryError".equals(nestedActivity.getProperty("type"))
                && (nestedActivity.getProperty("errorCode") == null 
                    || errorCode.equals(nestedActivity.getProperty("errorCode")))) {
          found = true;
          catchingActivity = nestedActivity;
        }
      }
      if (!found) {
        if (outgoingExecution.isConcurrent()) {
          outgoingExecution = outgoingExecution.getParent();
        } else if (outgoingExecution.isScope()) {
          catchingActivity = catchingActivity.getParentActivity();
          outgoingExecution = outgoingExecution.getParent();
        } 
      }
    }
    
    if (found) {
      outgoingExecution.executeActivity(catchingActivity);
    } else { // no matching catch found, going one level up in process hierarchy
      ActivityExecution superSuperExecution = getSuperExecution(superExecution);
      if (superSuperExecution != null) {
        executeCatchInSuperProcess(errorCode, superSuperExecution);
      } else {
        // TODO maybe throw BpmnError?
        throw new ActivitiException("No catching boundary event found for error with errorCode '" 
                + errorCode + "', neither in same process nor in parent process");
      }
    }
  }
  
  protected static ActivityExecution getSuperExecution(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    return executionEntity.getProcessInstance().getSuperExecution();
  }
  
  protected static void executeCatchInSameProcess(String borderEventActivityId, ActivityExecution execution) {
    ProcessDefinitionImpl processDefinition = ((ExecutionEntity) execution).getProcessDefinition();
    ActivityImpl borderEventActivity = processDefinition.findActivity(borderEventActivityId);
    if (borderEventActivity == null) {
      throw new ActivitiException(borderEventActivityId + " not found in process definition");
    }
    
    ActivityImpl catchingScope = borderEventActivity.getParentActivity();
    if (catchingScope == null) {
      throw new ActivitiException(borderEventActivityId + " is suppossed to be a nested activity, " 
              + "but no parent activity can be found.");
    }
    
    boolean matchingParentFound = false;
    ActivityExecution leavingExecution = execution;
    ActivityImpl currentActivity = (ActivityImpl) execution.getActivity();
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
      leavingExecution.executeActivity(borderEventActivity);
    } else {
      throw new ActivitiException("No matching parent execution for activity " + borderEventActivityId + " found");
    }
  }
  
}

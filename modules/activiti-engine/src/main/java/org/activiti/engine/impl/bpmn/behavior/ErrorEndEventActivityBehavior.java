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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * @author Joram Barrez
 */
public class ErrorEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  protected static final Logger LOG = Logger.getLogger(ErrorEndEventActivityBehavior.class.getName());
  protected String borderEventActivityId; // the nested activity representing the boundary event
  protected String errorCode;
  
  public ErrorEndEventActivityBehavior(String errorCode) {
    this.errorCode = errorCode;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    // TODO: merge two approaches (super process / regular process approach)
    
    // The borderEventActivityId is set during parsing (for performance reasons)
    // However, this only works on one process level (and not for call activities)
    // This is why there is a check for super processes  
    // in case the nested activity id has  not been set
    
    if (borderEventActivityId != null) {
      executeCatchInSameProcess(execution);
    } else { 
      ActivityExecution superExecution = getSuperExecution(execution);
      if (superExecution != null) {
        executeCatchInSuperProcess(superExecution);
      } else {
        // TODO Shouldn't this be an exception as in executeCatchInSuperProcess() or is this dead code?
        LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
                + errorCode + "', but no catching boundary event was defined. "
                +   "Execution will simply be ended (none end event semantics).");
        execution.end();
      }
    }
    
  }

  protected void executeCatchInSuperProcess(ActivityExecution superExecution) {
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
        executeCatchInSuperProcess(superSuperExecution);
      } else {
        // TODO maybe throw BpmnError?
        throw new ActivitiException("No catching boundary event found for error with errorCode '" 
                + errorCode + "', neither in same process nor in parent process");
      }
    }
  }
  
  protected ActivityExecution getSuperExecution(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    return executionEntity.getProcessInstance().getSuperExecution();
  }
  
  protected void executeCatchInSameProcess(ActivityExecution execution) {
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
  
  public String getBorderEventActivityId() {
    return borderEventActivityId;
  }
  public void setBorderEventActivityId(String borderEventActivityId) {
    this.borderEventActivityId = borderEventActivityId;
  }
  public String getErrorCode() {
    return errorCode;
  }
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
  
}

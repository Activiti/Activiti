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
package org.activiti.engine.impl.bpmn;

import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Joram Barrez
 */
public class ErrorEndEventActivityBehavior extends AbstractBpmnActivity {
  
  protected static final Logger LOG = Logger.getLogger(ErrorEndEventActivityBehavior.class.getName());
  protected String borderEventActivityId; // the nested activity representing the structure of the boundary event
  protected String errorCode;
  
  public ErrorEndEventActivityBehavior(String errorCode) {
    this.errorCode = errorCode;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    if (borderEventActivityId == null) {
      LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
              + errorCode + "', but no catching boundary event was defined. "
              +		"Execution will simply be ended (none end event semantics).");
      execution.end();
    } else {
      
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
      ActivityExecution parentExecution = execution;
      ActivityImpl parentActivity = (ActivityImpl) execution.getActivity().getParent();
      
      // Traverse parents until one is found that is a scope 
      // and matches the activity the boundary event is defined on
      while(!matchingParentFound && parentExecution != null && parentActivity != null) {
        if (parentExecution.isScope() 
                && !parentExecution.isConcurrent() 
                && parentActivity.getId().equals(catchingScope.getId())) {
          matchingParentFound = true;
        } else if (parentExecution.isConcurrent()) {
          parentExecution = parentExecution.getParent();
        } else if (parentExecution.isScope()) {
          parentActivity = parentActivity.getParentActivity();
          parentExecution = parentExecution.getParent();
        } 
        
      }
      
      // Execute the nested activity (== boundary event) in the parent execution
      if (matchingParentFound && parentExecution != null) {
        parentExecution.executeActivity(borderEventActivity);
      } else {
        throw new ActivitiException("No matching parent execution for activity " + borderEventActivityId + " found");
      }
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

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

package org.activiti.rest.api.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.application.ActivitiRestServicesApplication;


/**
 * @author Frederik Heremans
 */
public class BaseTaskVariableResource extends SecuredResource {
  
  
  public RestVariable getVariableFromRequest(boolean includeBinary) {
    String taskId = getAttribute("taskId");
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }
    
    String variableName = getAttribute("variableName");
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("The variableName cannot be null");
    }
    
    boolean variableFound = false;
    Object value = null;
    RestVariableScope variableScope = RestVariable.getScopeFromString(getQueryParameter("scope", getQuery()));
    if(variableScope == null) {
      // First, check local variables (which have precedence when no scope is supplied)
      if(ActivitiUtil.getTaskService().hasVariable(taskId, variableName)) {
        value = ActivitiUtil.getTaskService().getVariable(taskId, variableName);
        variableScope = RestVariableScope.LOCAL;
        variableFound = true;
      } else {
        // Revert to execution-variable when not present local on the task
        Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        if(task.getExecutionId() != null && ActivitiUtil.getRuntimeService().hasVariable(task.getExecutionId(), variableName)) {
          value = ActivitiUtil.getRuntimeService().getVariable(task.getExecutionId(), variableName);
          variableScope = RestVariableScope.GLOBAL;
          variableFound = true;
        }
      }
      
    } else if(variableScope == RestVariableScope.GLOBAL) {
      Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
      if(task.getExecutionId() != null && ActivitiUtil.getRuntimeService().hasVariable(task.getExecutionId(), variableName)) {
        value = ActivitiUtil.getRuntimeService().getVariable(task.getExecutionId(), variableName);
        variableFound = true;
      }
      
    } else if(variableScope == RestVariableScope.LOCAL) {
      if(ActivitiUtil.getTaskService().hasVariableLocal(taskId, variableName)) {
        value = ActivitiUtil.getTaskService().getVariableLocal(taskId, variableName);
        variableFound = true;
      }
    }
    
    if(!variableFound) {
        throw new ActivitiObjectNotFoundException("Task '" + taskId + "' doesn't have a variable with name: '" + variableName + "'.", VariableInstanceEntity.class);
    } else {
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
              .createRestVariable(this, variableName, value, variableScope, taskId, null, includeBinary);
    }
  }
  
  protected boolean hasVariableOnScope(String taskId, String variableName, RestVariableScope scope) {
    boolean variableFound = false;
      
    if(scope == RestVariableScope.GLOBAL) {
      Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
      if(task.getExecutionId() != null && ActivitiUtil.getRuntimeService().hasVariable(task.getExecutionId(), variableName)) {
        variableFound = true;
      }
      
    } else if(scope == RestVariableScope.LOCAL) {
      if(ActivitiUtil.getTaskService().hasVariableLocal(taskId, variableName)) {
        variableFound = true;
      }
    }
    return variableFound;
  }
}

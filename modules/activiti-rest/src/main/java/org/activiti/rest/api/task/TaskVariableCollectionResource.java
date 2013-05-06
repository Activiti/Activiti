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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskVariableCollectionResource extends BaseTaskVariableResource {

  @Get
  public List<RestVariable> getVariables() {
    if (authenticate() == false)
      return null;
    
    List<RestVariable> result = new ArrayList<RestVariable>();
    Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();
    
    // Check if it's a valid task to get the variables for
    Task task = getTaskFromRequest();
    
    RestVariableScope variableScope = RestVariable.getScopeFromString(getQueryParameter("scope", getQuery()));
    if(variableScope == null) {
      // Use both local and global variables
      addLocalVariables(task, variableMap);
      addGlobalVariables(task, variableMap);
    } else if(variableScope == RestVariableScope.GLOBAL) {
      addGlobalVariables(task, variableMap);
    } else if(variableScope == RestVariableScope.LOCAL) {
      addLocalVariables(task, variableMap);
    }
    
    // Get unique variables from map
    result.addAll(variableMap.values());
    return result;
  }
  
  protected void addGlobalVariables(Task task, Map<String, RestVariable> variableMap) {
    if(task.getExecutionId() != null) {
      Map<String, Object> rawVariables = ActivitiUtil.getRuntimeService().getVariables(task.getExecutionId());
      List<RestVariable> globalVariables = getApplication(ActivitiRestServicesApplication.class)
              .getRestResponseFactory().createRestVariables(this, rawVariables, task.getId(), null, RestVariableScope.GLOBAL);
      
      // Overlay global variables over local ones. In case they are present the values are not overridden, 
      // since local variables get precedence over global ones at all times.
      for(RestVariable var : globalVariables) {
        if(!variableMap.containsKey(var.getName())) {
          variableMap.put(var.getName(), var);
        }
      }
    }
  }

  protected void addLocalVariables(Task task, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = ActivitiUtil.getTaskService().getVariablesLocal(task.getId());
    List<RestVariable> localVariables = getApplication(ActivitiRestServicesApplication.class)
            .getRestResponseFactory().createRestVariables(this, rawVariables, task.getId(), null, RestVariableScope.LOCAL);

    for(RestVariable var : localVariables) {
      variableMap.put(var.getName(), var);
    }
  }

  /**
   * Get valid task from request. Throws exception if task doen't exist or if task id is not provided.
   */
  protected Task getTaskFromRequest() {
    String taskId = getAttribute("taskId");

    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }

    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if (task == null) {
      throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskId + "'.", Task.class);
    }
    return task;
  }
  
  @Post
  public RestVariable createVariable(RestVariable restVariable) {
    if (authenticate() == false)
      return null;
    
    // TODO: check if request is multipart-form
    if(restVariable.getName() == null) {
      throw new ActivitiIllegalArgumentException("Variable name is required");
    }
    String taskId = getAttribute("taskId");
    if(taskId == null) {
      throw new ActivitiIllegalArgumentException("TaskId is required");
    }

    // Figure out scope, revert to local is omitted
    RestVariableScope scope = restVariable.getVariableScope();
    if(scope == null) {
      scope = RestVariableScope.LOCAL;
    }
    
    // POST can only be done on new variables. Existing variables should be updated using PUT
    if(hasVariableOnScope(taskId, restVariable.getName(), scope)) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), "Variable '" + restVariable.getName() + "' is already present on task '" + taskId + "'.", null, null));
    }
    
    Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .getVariableValue(restVariable);
    
    if(scope == RestVariableScope.LOCAL) {
      ActivitiUtil.getTaskService().setVariableLocal(taskId, restVariable.getName(), actualVariableValue);
    } else {
      Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
      if(task.getExecutionId() != null) {
        // Explicitly set on execution, setting non-local variable on task will override local-variable if exists
        ActivitiUtil.getRuntimeService().setVariable(task.getExecutionId(), restVariable.getName(), actualVariableValue);
      } else {
        // Standalone task, no global variables possible
        throw new ActivitiIllegalArgumentException("Cannot set global variable '" + restVariable.getName() + "' on task '" + taskId +"', task is not part of process.");
      }
    }

    // Return created-status and variable representation
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
             .createRestVariable(this, restVariable.getName(), actualVariableValue, scope, taskId, null, false);
  }
  
}

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

package org.activiti.rest.service.api.runtime.task;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskVariableResource extends TaskVariableBaseResource {
  
  @Autowired
  protected ObjectMapper objectMapper;

  @RequestMapping(value="/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.GET, produces="application/json")
  public RestVariable getVariable(@PathVariable("taskId") String taskId, 
      @PathVariable("variableName") String variableName, @RequestParam(value="scope", required=false) String scope,
      HttpServletRequest request, HttpServletResponse response) {
    
    return getVariableFromRequest(taskId, variableName, scope, false);
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.PUT, produces="application/json")
  public RestVariable updateVariable(@PathVariable("taskId") String taskId, 
      @PathVariable("variableName") String variableName, @RequestParam(value="scope", required=false) String scope,
      HttpServletRequest request) {
    
    Task task = getTaskFromRequest(taskId);
    
    RestVariable result = null;
    if (request instanceof MultipartHttpServletRequest) {
      result = setBinaryVariable((MultipartHttpServletRequest) request, task, false);
      
      if (!result.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }
      
    } else {
      
      RestVariable restVariable = null;
      
      try {
        restVariable = objectMapper.readValue(request.getInputStream(), RestVariable.class);
      } catch (Exception e) {
        throw new ActivitiIllegalArgumentException("Error converting request body to RestVariable instance", e);
      }
      
      if (restVariable == null) {
        throw new ActivitiException("Invalid body was supplied");
      }
      if (!restVariable.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }
      
      result = setSimpleVariable(restVariable, task, false);
    }
    return result;
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/variables/{variableName}", method = RequestMethod.DELETE)
  public void deleteVariable(@PathVariable("taskId") String taskId, 
      @PathVariable("variableName") String variableName, 
      @RequestParam(value="scope", required=false) String scopeString, HttpServletResponse response) {
    
    Task task = getTaskFromRequest(taskId);
    
    // Determine scope
    RestVariableScope scope = RestVariableScope.LOCAL;
    if (scopeString != null) {
      scope = RestVariable.getScopeFromString(scopeString);
    }

    if (!hasVariableOnScope(task, variableName, scope)) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a variable '" + 
          variableName + "' in scope " + scope.name().toLowerCase(), VariableInstanceEntity.class);
    }
    
    if (scope == RestVariableScope.LOCAL) {
      taskService.removeVariableLocal(task.getId(), variableName);
    } else {
      // Safe to use executionId, as the hasVariableOnScope whould have stopped a global-var update on standalone task
      runtimeService.removeVariable(task.getExecutionId(), variableName);
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

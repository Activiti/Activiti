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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.exception.ActivitiContentNotSupportedException;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;


/**
 * @author Frederik Heremans
 */
public class TaskVariableBaseResource extends TaskBaseResource {

  @Autowired
  protected Environment env;

  @Autowired
  protected RuntimeService runtimeService;

  protected boolean isSerializableVariableAllowed;

  @PostConstruct
  protected void postConstruct() {
    isSerializableVariableAllowed = env.getProperty("rest.variables.allow.serializable", Boolean.class, true);
  }
  
  public RestVariable getVariableFromRequest(String taskId, String variableName, 
      String scope, boolean includeBinary) {
    
    boolean variableFound = false;
    Object value = null;
    RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
    if (variableScope == null) {
      // First, check local variables (which have precedence when no scope is supplied)
      if (taskService.hasVariableLocal(taskId, variableName)) {
        value = taskService.getVariableLocal(taskId, variableName);
        variableScope = RestVariableScope.LOCAL;
        variableFound = true;
      } else {
        // Revert to execution-variable when not present local on the task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task.getExecutionId() != null && runtimeService.hasVariable(task.getExecutionId(), variableName)) {
          value = runtimeService.getVariable(task.getExecutionId(), variableName);
          variableScope = RestVariableScope.GLOBAL;
          variableFound = true;
        }
      }
      
    } else if(variableScope == RestVariableScope.GLOBAL) {
      Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
      if (task.getExecutionId() != null && runtimeService.hasVariable(task.getExecutionId(), variableName)) {
        value = runtimeService.getVariable(task.getExecutionId(), variableName);
        variableFound = true;
      }
      
    } else if (variableScope == RestVariableScope.LOCAL) {
      if (taskService.hasVariableLocal(taskId, variableName)) {
        value = taskService.getVariableLocal(taskId, variableName);
        variableFound = true;
      }
    }
    
    if (!variableFound) {
        throw new ActivitiObjectNotFoundException("Task '" + taskId + "' doesn't have a variable with name: '" + variableName + "'.", VariableInstanceEntity.class);
    } else {
      return restResponseFactory.createRestVariable(variableName, value, variableScope, 
          taskId, RestResponseFactory.VARIABLE_TASK, includeBinary);
    }
  }
  
  protected boolean hasVariableOnScope(Task task, String variableName, RestVariableScope scope) {
    boolean variableFound = false;
      
    if (scope == RestVariableScope.GLOBAL) {
      if (task.getExecutionId() != null && runtimeService.hasVariable(task.getExecutionId(), variableName)) {
        variableFound = true;
      }
      
    } else if (scope == RestVariableScope.LOCAL) {
      if (taskService.hasVariableLocal(task.getId(), variableName)) {
        variableFound = true;
      }
    }
    return variableFound;
  }
  
  protected RestVariable setBinaryVariable(MultipartHttpServletRequest request, Task task, 
      boolean isNew) {
    
    // Validate input and set defaults
    if (request.getFileMap().size() == 0) {
      throw new ActivitiIllegalArgumentException("No file content was found in request body.");
    }
    
    // Get first file in the map, ignore possible other files
    MultipartFile file = request.getFile(request.getFileMap().keySet().iterator().next());
    
    if (file == null) {
      throw new ActivitiIllegalArgumentException("No file content was found in request body.");
    }
    
    String variableScope = null;
    String variableName = null;
    String variableType = null;
    
    Map<String, String[]> paramMap = request.getParameterMap();
    for (String parameterName : paramMap.keySet()) {
      
      if (paramMap.get(parameterName).length > 0) {
      
        if (parameterName.equalsIgnoreCase("scope")) {
          variableScope = paramMap.get(parameterName)[0];
          
        } else if (parameterName.equalsIgnoreCase("name")) {
          variableName = paramMap.get(parameterName)[0];
          
        } else if (parameterName.equalsIgnoreCase("type")) {
          variableType = paramMap.get(parameterName)[0];
        }
      }
    }
    
    try {
      
      if (variableName == null) {
        throw new ActivitiIllegalArgumentException("No variable name was found in request body.");
      }
      
      if (variableType != null) {
        if (!RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variableType) && !RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
          throw new ActivitiIllegalArgumentException("Only 'binary' and 'serializable' are supported as variable type.");
        }
      } else {
        variableType = RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE;
      }
      
      RestVariableScope scope = RestVariableScope.LOCAL;
      if (variableScope != null) {
        scope = RestVariable.getScopeFromString(variableScope);
      }
      
      if (variableType.equals(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE)) {
        // Use raw bytes as variable value
        byte[] variableBytes = IOUtils.toByteArray(file.getInputStream());
        setVariable(task, variableName, variableBytes, scope, isNew);
        
      } else if (isSerializableVariableAllowed) {
        // Try deserializing the object
        ObjectInputStream stream = new ObjectInputStream(file.getInputStream());
        Object value = stream.readObject();
        setVariable(task, variableName, value, scope, isNew);
        stream.close();
      } else {
        throw new ActivitiContentNotSupportedException("Serialized objects are not allowed");
      }
      
      return restResponseFactory.createBinaryRestVariable(variableName, scope, variableType, task.getId(), null, null);
      
    } catch (IOException ioe) {
      throw new ActivitiIllegalArgumentException("Error getting binary variable", ioe);
    } catch (ClassNotFoundException ioe) {
      throw new ActivitiContentNotSupportedException("The provided body contains a serialized object for which the class is nog found: " + ioe.getMessage());
    }
    
  }
  
  protected RestVariable setSimpleVariable(RestVariable restVariable, Task task, boolean isNew) {
    if (restVariable.getName() == null) {
      throw new ActivitiIllegalArgumentException("Variable name is required");
    }

    // Figure out scope, revert to local is omitted
    RestVariableScope scope = restVariable.getVariableScope();
    if (scope == null) {
      scope = RestVariableScope.LOCAL;
    }
    
    Object actualVariableValue = restResponseFactory.getVariableValue(restVariable);
    setVariable(task, restVariable.getName(), actualVariableValue, scope, isNew);
    
    return restResponseFactory.createRestVariable(restVariable.getName(), actualVariableValue, scope, 
        task.getId(), RestResponseFactory.VARIABLE_TASK, false);
  }
  
  protected void setVariable(Task task, String name, Object value, RestVariableScope scope, boolean isNew) {
    // Create can only be done on new variables. Existing variables should be updated using PUT
    boolean hasVariable = hasVariableOnScope(task, name, scope);
    if (isNew && hasVariable) {
      throw new ActivitiException("Variable '" + name + "' is already present on task '" + task.getId() + "'.");
    }
    
    if (!isNew && !hasVariable) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a variable with name: '"+ name + "'.", null);
    }
    
    if (scope == RestVariableScope.LOCAL) {
      taskService.setVariableLocal(task.getId(), name, value);
    } else {
      if (task.getExecutionId() != null) {
        // Explicitly set on execution, setting non-local variable on task will override local-variable if exists
        runtimeService.setVariable(task.getExecutionId(), name, value);
      } else {
        // Standalone task, no global variables possible
        throw new ActivitiIllegalArgumentException("Cannot set global variable '" + name + "' on task '" + 
            task.getId() +"', task is not part of process.");
      }
    }
  }
}

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

package org.activiti.rest.service.api.runtime.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.exception.ActivitiConflictException;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tijs Rademakers
 */
public class BaseVariableCollectionResource extends BaseExecutionVariableResource {
  
  @Autowired
  protected ObjectMapper objectMapper;

  protected List<RestVariable> processVariables(Execution execution, String scope, int variableType) {
    List<RestVariable> result = new ArrayList<RestVariable>();
    Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();
    
    // Check if it's a valid execution to get the variables for
    RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
    
    if (variableScope == null) {
      // Use both local and global variables
      addLocalVariables(execution, variableType, variableMap);
      addGlobalVariables(execution, variableType, variableMap);
      
    } else if (variableScope == RestVariableScope.GLOBAL) {
      addGlobalVariables(execution, variableType, variableMap);
      
    } else if (variableScope == RestVariableScope.LOCAL) {
      addLocalVariables(execution, variableType, variableMap);
    }
    
    // Get unique variables from map
    result.addAll(variableMap.values());
    return result;
  }
  
  public void deleteAllLocalVariables(Execution execution, HttpServletResponse response) {
    Collection<String> currentVariables = runtimeService.getVariablesLocal(execution.getId()).keySet();
    runtimeService.removeVariablesLocal(execution.getId(), currentVariables);
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  protected Object createExecutionVariable(Execution execution, boolean override, int variableType, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Object result = null;
    if (request instanceof MultipartHttpServletRequest) {
      result = setBinaryVariable((MultipartHttpServletRequest) request, execution, variableType, true);
    } else {
      
      List<RestVariable> inputVariables = new ArrayList<RestVariable>();
      List<RestVariable> resultVariables = new ArrayList<RestVariable>();
      result = resultVariables;
      
      try {
        @SuppressWarnings("unchecked")
        List<Object> variableObjects = (List<Object>) objectMapper.readValue(request.getInputStream(), List.class);
        for (Object restObject : variableObjects) {
          RestVariable restVariable = objectMapper.convertValue(restObject, RestVariable.class);
          inputVariables.add(restVariable);
        }
      } catch (Exception e) {
        throw new ActivitiIllegalArgumentException("Failed to serialize to a RestVariable instance", e);
      }
      
      if (inputVariables == null || inputVariables.size() == 0) {
        throw new ActivitiIllegalArgumentException("Request didn't contain a list of variables to create.");
      }
      
      RestVariableScope sharedScope = null;
      RestVariableScope varScope = null;
      Map<String, Object> variablesToSet = new HashMap<String, Object>();
      
      for (RestVariable var : inputVariables) {
        // Validate if scopes match
        varScope = var.getVariableScope();
        if (var.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required");
        }
        
        if (varScope == null) {
          varScope = RestVariableScope.LOCAL;
        }
        if (sharedScope == null) {
          sharedScope = varScope;
        }
        if (varScope != sharedScope) {
          throw new ActivitiIllegalArgumentException("Only allowed to update multiple variables in the same scope.");
        }
        
        if (!override && hasVariableOnScope(execution, var.getName(), varScope)) {
          throw new ActivitiConflictException("Variable '" + var.getName() + "' is already present on execution '" + execution.getId() + "'.");
        }
        
        Object actualVariableValue = restResponseFactory.getVariableValue(var);
        variablesToSet.put(var.getName(), actualVariableValue);
        resultVariables.add(restResponseFactory.createRestVariable(var.getName(), actualVariableValue, varScope, 
            execution.getId(), variableType, false));
      }
      
      if (!variablesToSet.isEmpty()) {
        if (sharedScope == RestVariableScope.LOCAL) {
          runtimeService.setVariablesLocal(execution.getId(), variablesToSet);
        } else {
          if (execution.getParentId() != null) {
            // Explicitly set on parent, setting non-local variables on execution itself will override local-variables if exists
            runtimeService.setVariables(execution.getParentId(), variablesToSet);
          } else {
            // Standalone task, no global variables possible
            throw new ActivitiIllegalArgumentException("Cannot set global variables on execution '" + execution.getId() +"', task is not part of process.");
          }
        }
      }
    }
    response.setStatus(HttpStatus.CREATED.value());
    return result;
  }
  
  protected void addGlobalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
    List<RestVariable> globalVariables = restResponseFactory.createRestVariables(rawVariables, 
        execution.getId(), variableType, RestVariableScope.GLOBAL);
    
    // Overlay global variables over local ones. In case they are present the values are not overridden, 
    // since local variables get precedence over global ones at all times.
    for (RestVariable var : globalVariables) {
      if (!variableMap.containsKey(var.getName())) {
        variableMap.put(var.getName(), var);
      }
    }
  }

  protected void addLocalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawLocalvariables = runtimeService.getVariablesLocal(execution.getId());
    List<RestVariable> localVariables = restResponseFactory.createRestVariables(rawLocalvariables, 
        execution.getId(), variableType, RestVariableScope.LOCAL);
    
    for (RestVariable var : localVariables) {
      variableMap.put(var.getName(), var);
    }
  }
}

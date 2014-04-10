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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskVariableCollectionResource extends TaskVariableBaseResource {

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
  
  @Post
  public Object createTaskVariable(Representation representation) {
    if (authenticate() == false)
      return null;
    
    Task task = getTaskFromRequest();
    Object result = null;
    if(MediaType.MULTIPART_FORM_DATA.isCompatible(representation.getMediaType())) {
      result = setBinaryVariable(representation, task, true);
    } else {
      // Since we accept both an array of RestVariables and a single RestVariable, we need to inspect the
      // body before passing on to the converterService
      try {
        List<RestVariable> variables = new ArrayList<RestVariable>();
        result = variables;
        
        RestVariable[] restVariables = getConverterService().toObject(representation, RestVariable[].class, this);
        if(restVariables == null || restVariables.length == 0) {
          throw new ActivitiIllegalArgumentException("Request didn't contain a list of variables to create.");
        }
        
        RestVariableScope sharedScope = null;
        RestVariableScope varScope = null;
        Map<String, Object> variablesToSet = new HashMap<String, Object>();
        
        RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
        for(RestVariable var : restVariables) {
          // Validate if scopes match
          varScope = var.getVariableScope();
          if(var.getName() == null) {
            throw new ActivitiIllegalArgumentException("Variable name is required");
          }
          
          if(varScope == null) {
            varScope = RestVariableScope.LOCAL;
          }
          if(sharedScope == null) {
            sharedScope = varScope;
          }
          if(varScope != sharedScope) {
            throw new ActivitiIllegalArgumentException("Only allowed to update multiple variables in the same scope.");
          }
          
          if(hasVariableOnScope(task, var.getName(), varScope)) {
            throw new ResourceException(new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), "Variable '" + var.getName() + "' is already present on task '" + task.getId() + "'.", null, null));
          }
          
          Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                  .getVariableValue(var);
          variablesToSet.put(var.getName(), actualVariableValue);
          variables.add(factory.createRestVariable(this, var.getName(), actualVariableValue, varScope, task.getId(), RestResponseFactory.VARIABLE_TASK, false));
        }
        
        if(variablesToSet.size() > 0) {
          if(sharedScope == RestVariableScope.LOCAL) {
            ActivitiUtil.getTaskService().setVariablesLocal(task.getId(), variablesToSet);
          } else {
            if(task.getExecutionId() != null) {
              // Explicitly set on execution, setting non-local variables on task will override local-variables if exists
              ActivitiUtil.getRuntimeService().setVariables(task.getExecutionId(), variablesToSet);
            } else {
              // Standalone task, no global variables possible
              throw new ActivitiIllegalArgumentException("Cannot set global variables on task '" + task.getId() +"', task is not part of process.");
            }
          }
        }
      } catch (IOException ioe) {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
      }
    }
    setStatus(Status.SUCCESS_CREATED);
    return result;
  }
  
  @Delete
  public void deleteAllLocalTaskVariables() {
  	if(!authenticate()) { return; }
  	
    Task task = getTaskFromRequest();
    Collection<String> currentVariables = ActivitiUtil.getTaskService().getVariablesLocal(task.getId()).keySet();
    ActivitiUtil.getTaskService().removeVariablesLocal(task.getId(), currentVariables);
    
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  protected void addGlobalVariables(Task task, Map<String, RestVariable> variableMap) {
    if(task.getExecutionId() != null) {
      Map<String, Object> rawVariables = ActivitiUtil.getRuntimeService().getVariables(task.getExecutionId());
      List<RestVariable> globalVariables = getApplication(ActivitiRestServicesApplication.class)
              .getRestResponseFactory().createRestVariables(this, rawVariables, task.getId(), RestResponseFactory.VARIABLE_TASK, RestVariableScope.GLOBAL);
      
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
            .getRestResponseFactory().createRestVariables(this, rawVariables, task.getId(), RestResponseFactory.VARIABLE_TASK, RestVariableScope.LOCAL);
    
    for(RestVariable var : localVariables) {
      variableMap.put(var.getName(), var);
    }
  }
}

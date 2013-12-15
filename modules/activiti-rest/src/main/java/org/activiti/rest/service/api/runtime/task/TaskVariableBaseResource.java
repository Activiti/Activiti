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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class TaskVariableBaseResource extends TaskBaseResource {
  
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
      if(ActivitiUtil.getTaskService().hasVariableLocal(taskId, variableName)) {
        value = ActivitiUtil.getTaskService().getVariableLocal(taskId, variableName);
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
      RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
      return responseFactory.createRestVariable(this, variableName, value, variableScope, taskId, RestResponseFactory.VARIABLE_TASK, includeBinary);
    }
  }
  
  protected boolean hasVariableOnScope(Task task, String variableName, RestVariableScope scope) {
    boolean variableFound = false;
      
    if(scope == RestVariableScope.GLOBAL) {
      if(task.getExecutionId() != null && ActivitiUtil.getRuntimeService().hasVariable(task.getExecutionId(), variableName)) {
        variableFound = true;
      }
      
    } else if(scope == RestVariableScope.LOCAL) {
      if(ActivitiUtil.getTaskService().hasVariableLocal(task.getId(), variableName)) {
        variableFound = true;
      }
    }
    return variableFound;
  }
  
  protected RestVariable setBinaryVariable(Representation representation, Task task, boolean isNew) {
    try {
      RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
      List<FileItem> items = upload.parseRepresentation(representation);
      
      String variableScope = null;
      String variableName = null;
      String variableType = null;
      FileItem uploadItem = null;
      
      for (FileItem fileItem : items) {
        if(fileItem.isFormField()) {
          if("scope".equals(fileItem.getFieldName())) {
            variableScope = fileItem.getString("UTF-8");
          } else if("name".equals(fileItem.getFieldName())) {
            variableName = fileItem.getString("UTF-8");
          } else if("type".equals(fileItem.getFieldName())) {
            variableType = fileItem.getString("UTF-8");
          }
        } else  if(fileItem.getName() != null) {
          uploadItem = fileItem;
        }
      }
      
      // Validate input and set defaults
      if(uploadItem == null) {
        throw new ActivitiIllegalArgumentException("No file content was found in request body.");
      }
      
      if(variableName == null) {
        throw new ActivitiIllegalArgumentException("No variable name was found in request body.");
      }
      
      if(variableType != null) {
        if(!RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variableType) && !RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
          throw new ActivitiIllegalArgumentException("Only 'binary' and 'serializable' are supported as variable type.");
        }
      } else {
        variableType = RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE;
      }
      
      RestVariableScope scope = RestVariableScope.LOCAL;
      if(variableScope != null) {
        scope = RestVariable.getScopeFromString(variableScope);
      }
      
      if(variableType.equals(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE)) {
        // Use raw bytes as variable value
        ByteArrayOutputStream variableOutput = new ByteArrayOutputStream(((Long)uploadItem.getSize()).intValue());
        IOUtils.copy(uploadItem.getInputStream(), variableOutput);
        setVariable(task, variableName, variableOutput.toByteArray(), scope, isNew);
      } else {
        // Try deserializing the object
        ObjectInputStream stream = new ObjectInputStream(uploadItem.getInputStream());
        Object value = stream.readObject();
        setVariable(task, variableName, value, scope, isNew);
        stream.close();
      }
      
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .createBinaryRestVariable(this, variableName, scope, variableType, task.getId(), null, null);
      
    } catch(FileUploadException fue) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, fue);
    } catch (IOException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
    } catch (ClassNotFoundException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "The provided body contains a serialized object for which the class is nog found: " + ioe.getMessage(), null, null);
    }
    
  }
  
  protected RestVariable setSimpleVariable(RestVariable restVariable, Task task, boolean isNew) {
    if(restVariable.getName() == null) {
      throw new ActivitiIllegalArgumentException("Variable name is required");
    }

    // Figure out scope, revert to local is omitted
    RestVariableScope scope = restVariable.getVariableScope();
    if(scope == null) {
      scope = RestVariableScope.LOCAL;
    }
    
    Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .getVariableValue(restVariable);
    setVariable(task, restVariable.getName(), actualVariableValue, scope, isNew);
    
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    return responseFactory.createRestVariable(this, restVariable.getName(), actualVariableValue, scope, task.getId(), RestResponseFactory.VARIABLE_TASK, false);
  }
  
  protected void setVariable(Task task, String name, Object value, RestVariableScope scope, boolean isNew) {
    // Create can only be done on new variables. Existing variables should be updated using PUT
    boolean hasVariable = hasVariableOnScope(task, name, scope);
    if(isNew && hasVariable) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), "Variable '" + name + "' is already present on task '" + task.getId() + "'.", null, null));
    }
    
    if(!isNew && !hasVariable) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a variable with name: '"+ name + "'.", null);
    }
    
    if(scope == RestVariableScope.LOCAL) {
      ActivitiUtil.getTaskService().setVariableLocal(task.getId(), name, value);
    } else {
      if(task.getExecutionId() != null) {
        // Explicitly set on execution, setting non-local variable on task will override local-variable if exists
        ActivitiUtil.getRuntimeService().setVariable(task.getExecutionId(), name, value);
      } else {
        // Standalone task, no global variables possible
        throw new ActivitiIllegalArgumentException("Cannot set global variable '" + name + "' on task '" + task.getId() +"', task is not part of process.");
      }
    }
  }
}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
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
  
  @Post
  public Object createTaskVariable(Representation representation) {
    if (authenticate() == false)
      return null;
    
    Task task = getTaskFromRequest();
    Object result = null;
    if(MediaType.MULTIPART_FORM_DATA.isCompatible(representation.getMediaType())) {
      result = createBinaryVariable(representation, task);
    } else {
      // Since we accept both an array of RestVariables and a single RestVariable, we need to inspect the
      // body before passing on to the converterService
      try {
        List<RestVariable> variables = new ArrayList<RestVariable>();
        result = variables;
        
        RestVariable[] restVariables = getConverterService().toObject(representation, RestVariable[].class, this);
        if(restVariables == null || restVariables.length == 0) {
          throw new ActivitiIllegalArgumentException("Request didn't cantain a list of variables to create.");
        }
        for(RestVariable var : restVariables) {
          variables.add(createSimpleVariable(var, task));
        }
      } catch (IOException ioe) {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
      }
    }
    setStatus(Status.SUCCESS_CREATED);
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

  protected RestVariable createBinaryVariable(Representation representation, Task task) {
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
        setVariable(task, variableName, variableOutput.toByteArray(), scope);
      } else {
        // Try deserializing the object
        ObjectInputStream stream = new ObjectInputStream(uploadItem.getInputStream());
        Object value = stream.readObject();
        setVariable(task, variableName, value, scope);
        stream.close();
      }
      
      return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .createBinaryRestVariable(this, variableName, scope, variableType, task.getId(), null);
      
    } catch(FileUploadException fue) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, fue);
    } catch (IOException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
    } catch (ClassNotFoundException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "The provided body contains a serialized object for which the class is nog found: " + ioe.getMessage(), null, null);
    }
    
  }
  
  protected RestVariable createSimpleVariable(RestVariable restVariable, Task task) {
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
    setVariable(task, restVariable.getName(), actualVariableValue, scope);
    

    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
             .createRestVariable(this, restVariable.getName(), actualVariableValue, scope, task.getId(), null, false);
  }
  
  protected void setVariable(Task task, String name, Object value, RestVariableScope scope) {
    // Create can only be done on new variables. Existing variables should be updated using PUT
    if(hasVariableOnScope(task, name, scope)) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), "Variable '" + name + "' is already present on task '" + task.getId() + "'.", null, null));
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
  
  protected void addLocalVariables(Task task, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = ActivitiUtil.getTaskService().getVariablesLocal(task.getId());
    List<RestVariable> localVariables = getApplication(ActivitiRestServicesApplication.class)
            .getRestResponseFactory().createRestVariables(this, rawVariables, task.getId(), null, RestVariableScope.LOCAL);
    
    for(RestVariable var : localVariables) {
      variableMap.put(var.getName(), var);
    }
  }
}

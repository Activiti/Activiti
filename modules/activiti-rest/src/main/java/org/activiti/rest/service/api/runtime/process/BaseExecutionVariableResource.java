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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
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
public class BaseExecutionVariableResource extends SecuredResource {
  
  protected RestVariable setBinaryVariable(Representation representation, Execution execution, boolean isNew) {
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
        setVariable(execution, variableName, variableOutput.toByteArray(), scope, isNew);
      } else {
        // Try deserializing the object
        ObjectInputStream stream = new ObjectInputStream(uploadItem.getInputStream());
        Object value = stream.readObject();
        setVariable(execution, variableName, value, scope, isNew);
        stream.close();
      }
      
      if(execution instanceof ProcessInstance && allowProcessInstanceUrl()) {
        return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .createBinaryRestVariable(this, variableName, scope, variableType, null, null, execution.getId());
      } else {
        return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .createBinaryRestVariable(this, variableName, scope, variableType, null, execution.getId(), null);
      }
      
    } catch(FileUploadException fue) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, fue);
    } catch (IOException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
    } catch (ClassNotFoundException ioe) {
      throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "The provided body contains a serialized object for which the class is nog found: " + ioe.getMessage(), null, null);
    }
    
  }
  
  protected RestVariable setSimpleVariable(RestVariable restVariable, Execution execution, boolean isNew) {
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
    setVariable(execution, restVariable.getName(), actualVariableValue, scope, isNew);
    

    return constructRestVariable(this, restVariable.getName(), actualVariableValue, scope, execution.getId(), false);
  }
  
  protected void setVariable(Execution execution, String name, Object value, RestVariableScope scope, boolean isNew) {
    // Create can only be done on new variables. Existing variables should be updated using PUT
    boolean hasVariable = hasVariableOnScope(execution, name, scope);
    if(isNew && hasVariable) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_CONFLICT.getCode(), "Variable '" + name + "' is already present on execution '" + execution.getId() + "'.", null, null));
    }
    
    if(!isNew && !hasVariable) {
      throw new ActivitiObjectNotFoundException("Execution '" + execution.getId() + "' doesn't have a variable with name: '"+ name + "'.", null);
    }
    
    if(scope == RestVariableScope.LOCAL) {
      ActivitiUtil.getRuntimeService().setVariableLocal(execution.getId(), name, value);
    } else {
      if(execution.getParentId() != null) {
        ActivitiUtil.getRuntimeService().setVariable(execution.getParentId(), name, value);
      } else {
        ActivitiUtil.getRuntimeService().setVariable(execution.getId(), name, value);
      }
    }
  }
  
  protected boolean hasVariableOnScope(Execution execution, String variableName, RestVariableScope scope) {
    boolean variableFound = false;
      
    if(scope == RestVariableScope.GLOBAL) {
      if(execution.getParentId() != null && ActivitiUtil.getRuntimeService().hasVariable(execution.getParentId(), variableName)) {
        variableFound = true;
      }
      
    } else if(scope == RestVariableScope.LOCAL) {
      if(ActivitiUtil.getRuntimeService().hasVariableLocal(execution.getId(), variableName)) {
        variableFound = true;
      }
    }
    return variableFound;
  }
  
  public RestVariable getVariableFromRequest(boolean includeBinary) {
    String executionId = getAttribute(getExecutionIdParameter());
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("The " + getExecutionIdParameter() + " cannot be null");
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
      if(ActivitiUtil.getRuntimeService().hasVariableLocal(executionId, variableName)) {
        value = ActivitiUtil.getRuntimeService().getVariableLocal(executionId, variableName);
        variableScope = RestVariableScope.LOCAL;
        variableFound = true;
      } else {
        Execution execution = ActivitiUtil.getRuntimeService().createExecutionQuery().executionId(executionId).singleResult();
        if(execution == null) {
          throw new ActivitiObjectNotFoundException("Could not find an execution with id '" + executionId + "'.", ProcessInstance.class);
        }
        if(execution.getParentId() != null) {
          value = ActivitiUtil.getRuntimeService().getVariable(executionId, execution.getParentId());
          variableScope = RestVariableScope.GLOBAL;
          variableFound = true;
        }
      }
    } else if(variableScope == RestVariableScope.GLOBAL) {
      // Use parent to get variables
      Execution execution = ActivitiUtil.getRuntimeService().createExecutionQuery().executionId(executionId).singleResult();
      if(execution == null) {
        throw new ActivitiObjectNotFoundException("Could not find an execution with id '" + executionId + "'.", ProcessInstance.class);
      }
      if(execution.getParentId() != null) {
        value = ActivitiUtil.getRuntimeService().getVariable(execution.getParentId(), variableName);
        variableScope = RestVariableScope.GLOBAL;
        variableFound = true;
      }
    } else if(variableScope == RestVariableScope.LOCAL) {
      value = ActivitiUtil.getRuntimeService().getVariableLocal(executionId, variableName);
      variableScope = RestVariableScope.LOCAL;
      variableFound = true;
    }
    
    if(!variableFound) {
        throw new ActivitiObjectNotFoundException("Execution '" + executionId + "' doesn't have a variable with name: '" + variableName + "'.", VariableInstanceEntity.class);
    } else {
      return constructRestVariable(this, variableName, value, variableScope, executionId, includeBinary);
    }
  }
  
  
  protected RestVariable constructRestVariable(SecuredResource securedResource, String variableName, Object value,
          RestVariableScope variableScope, String executionId, boolean includeBinary) {
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestVariable(this, variableName, value, variableScope, executionId, RestResponseFactory.VARIABLE_EXECUTION, includeBinary);
  }

  /**
   * Get valid execution from request. Throws exception if execution doen't exist or if execution id is not provided.
   */
  protected Execution getExecutionFromRequest() {
    String executionId = getAttribute(getExecutionIdParameter());
    
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("The " + getExecutionIdParameter() +" cannot be null");
    }
    
    Execution execution = ActivitiUtil.getRuntimeService().createExecutionQuery().executionId(executionId).singleResult();
    if (execution == null) {
      throw new ActivitiObjectNotFoundException("Could not find an execution with id '" + executionId + "'.", Execution.class);
    }
    return execution;
  }
  
  protected String getExecutionIdParameter() {
    return "executionId";
  }
  
  protected boolean allowProcessInstanceUrl() {
    return false;
  }
}

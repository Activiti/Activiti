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

import java.io.IOException;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class ExecutionVariableResource extends BaseExecutionVariableResource {

  @Get
  public RestVariable getVariable() {
    if (authenticate() == false)
      return null;
    
    return getVariableFromRequest(false);
  }
  
  @Put
  public RestVariable updateVariable(Representation representation) {
    if (authenticate() == false)
      return null;
    
    String variableName = getAttribute("variableName");
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("The variableName cannot be null");
    }
    
    Execution execution = getExecutionFromRequest();
    
    RestVariable result = null;
    if(representation.getMediaType() != null && MediaType.MULTIPART_FORM_DATA.isCompatible(representation.getMediaType())) {
      result = setBinaryVariable(representation, execution, false);
      
      if(!result.getName().equals(variableName)) {
        throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
      }
    } else {
      try {
        RestVariable restVariable = getConverterService().toObject(representation, RestVariable.class, this);
        if(restVariable == null) {
          throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(), "Invalid body was supplied", null, null));
        }
        if(!restVariable.getName().equals(variableName)) {
          throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
        }
        
        result = setSimpleVariable(restVariable, execution, false);
      } catch(IOException ioe) {
        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ioe);
      }
    }
    return result;
  }
  
  @Delete
  public void deleteVariable() {
    if (authenticate() == false)
      return;

    Execution execution = getExecutionFromRequest();
    
    String variableName = getAttribute("variableName");
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("The variableName cannot be null");
    }
    
    // Determine scope
    String scopeString = getQueryParameter("scope", getQuery());
    RestVariableScope scope = RestVariableScope.LOCAL;
    if(scopeString != null) {
      scope = RestVariable.getScopeFromString(scopeString);
    }

    if(!hasVariableOnScope(execution, variableName, scope)) {
      throw new ActivitiObjectNotFoundException("Execution '" + execution.getId() + "' doesn't have a variable '" + variableName + "' in scope " + scope.name().toLowerCase(), VariableInstanceEntity.class);
    }
    
    if(scope == RestVariableScope.LOCAL) {
      ActivitiUtil.getRuntimeService().removeVariableLocal(execution.getId(), variableName);
    } else {
      // Safe to use parentId, as the hasVariableOnScope would have stopped a global-var update on a root-execution
      ActivitiUtil.getRuntimeService().removeVariable(execution.getParentId(), variableName);
    }
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

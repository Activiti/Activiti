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

package org.activiti.rest.service.api.history;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceVariableDataResource extends SecuredResource {

  @Get
  public InputRepresentation getVariableData() {
    if (authenticate() == false)
      return null;

    try {
      InputStream dataStream = null;
      MediaType mediaType = null;
      RestVariable variable = getVariableFromRequest(true);
      if(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
        dataStream = new ByteArrayInputStream((byte[]) variable.getValue());
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
        outputStream.writeObject(variable.getValue());
        outputStream.close();
        dataStream = new ByteArrayInputStream(buffer.toByteArray());
        mediaType = MediaType.APPLICATION_JAVA_OBJECT;
        
      } else {
        throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
      }
      return new InputRepresentation(dataStream, mediaType);
    } catch(IOException ioe) {
      // Re-throw IOException
      throw new ActivitiException("Unexpected exception getting variable data", ioe);
    }
  }
  
  public RestVariable getVariableFromRequest(boolean includeBinary) {
    String taskId = getAttribute("taskId");
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }
    
    String variableName = getAttribute("variableName");
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("The variableName cannot be null");
    }
    
    RestVariableScope variableScope = RestVariable.getScopeFromString(getQueryParameter("scope", getQuery()));
    HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery().taskId(taskId);
    
    if (variableScope != null) {
      if (variableScope == RestVariableScope.GLOBAL) {
        taskQuery.includeProcessVariables();
      } else {
        taskQuery.includeTaskLocalVariables();
      }
    } else {
      taskQuery.includeTaskLocalVariables().includeProcessVariables();
    }
    
    HistoricTaskInstance taskObject = taskQuery.singleResult();
    
    if (taskObject == null) {
      throw new ActivitiObjectNotFoundException("Historic task instance '" + taskId + "' couldn't be found.", HistoricTaskInstanceEntity.class);
    }
    
    Object value = null;
    if (variableScope != null) {
      if (variableScope == RestVariableScope.GLOBAL) {
        value = taskObject.getProcessVariables().get(variableName);
      } else {
        value = taskObject.getTaskLocalVariables().get(variableName);
      }
    } else {
      // look for local task variables first
      if (taskObject.getTaskLocalVariables().containsKey(variableName)) {
        value = taskObject.getTaskLocalVariables().get(variableName);
      } else {
        value = taskObject.getProcessVariables().get(variableName);
      }
    }
    
    if (value == null) {
        throw new ActivitiObjectNotFoundException("Historic task instance '" + taskId + "' variable value for " + variableName + " couldn't be found.", VariableInstanceEntity.class);
    } else {
      RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
      return responseFactory.createRestVariable(this, variableName, value, null, taskId, 
          RestResponseFactory.VARIABLE_HISTORY_TASK, includeBinary);
    }
  }
}

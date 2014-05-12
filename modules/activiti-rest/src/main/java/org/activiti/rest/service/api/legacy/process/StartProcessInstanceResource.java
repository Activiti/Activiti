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

package org.activiti.rest.service.api.legacy.process;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class StartProcessInstanceResource extends SecuredResource {
  
  @Post
  public StartProcessInstanceResponse startProcessInstance(Representation entity) {
    try {
      if(authenticate() == false) return null;
      
      String startParams = entity.getText();
      JsonNode startJSON = new ObjectMapper().readTree(startParams);
      String processDefinitionKey = startJSON.path("processDefinitionKey").textValue();
      String processDefinitionId = null;
      if (processDefinitionKey == null) {
        processDefinitionId = startJSON.path("processDefinitionId").textValue();
      }
      JsonNode businessKeyJson = startJSON.path("businessKey");
      String businessKey = null;
      if(businessKeyJson != null) {
        businessKey = businessKeyJson.textValue();
      }
      
      Map<String, Object> variables = retrieveVariables(startJSON);
      variables.remove("processDefinitionId");
      variables.remove("processDefinitionKey");
      variables.remove("businessKey");
      
      ProcessInstance processInstance = null;
      if (processDefinitionKey != null) {
        processInstance = ActivitiUtil.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
      }
      else {
        processInstance = ActivitiUtil.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, variables);
      }
      StartProcessInstanceResponse response = new StartProcessInstanceResponse(processInstance);
      return response;
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException("Failed to retrieve the process definition parameters", e);
    }
    
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}

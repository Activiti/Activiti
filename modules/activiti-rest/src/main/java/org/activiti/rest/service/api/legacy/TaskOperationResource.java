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

package org.activiti.rest.service.api.legacy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.apache.commons.lang3.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class TaskOperationResource extends SecuredResource {
  
  @Put
  public ObjectNode executeTaskOperation(Representation entity) {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    String operation = (String) getRequest().getAttributes().get("operation");
      
    if ("claim".equals(operation)) {
      try {
        ActivitiUtil.getTaskService().claim(taskId, loggedInUser);
      } catch(ActivitiTaskAlreadyClaimedException atece) {
        // Explicitally throw an exception that is not the ActivitiTaskAlreadyClaimedException, as this causes a 409 instead of a 500
        throw new IllegalStateException(atece);
      }
    } else if ("unclaim".equals(operation)) {
      ActivitiUtil.getTaskService().claim(taskId, null);
    } else if ("complete".equals(operation)) {
      
      Map<String, String> variables = new HashMap<String, String>();
      try {
        if (entity != null) {
          String startParams = entity.getText();
          if (StringUtils.isNotEmpty(startParams)) {
            JsonNode startJSON = new ObjectMapper().readTree(startParams);
            Iterator<String> itName = startJSON.fieldNames();
            while(itName.hasNext()) {
              String name = itName.next();
              JsonNode valueNode = startJSON.path(name);
              variables.put(name, valueNode.asText());
            }
          }
        }
      } catch(Exception e) {
        if(e instanceof ActivitiException) {
          throw (ActivitiException) e;
        }
        throw new ActivitiException("Did not receive the operation parameters", e);
      }
      
      variables.remove("taskId");
      ActivitiUtil.getFormService().submitTaskFormData(taskId, variables);
      
    } else if ("assign".equals(operation)) {
      String userId = null;
      try {
        String startParams = entity.getText();
        JsonNode startJSON = new ObjectMapper().readTree(startParams);
        userId = startJSON.path("userId").textValue();
      } catch(Exception e) {
        throw new ActivitiException("Did not assign the operation parameters", e);
      }
      ActivitiUtil.getTaskService().setAssignee(taskId, userId);
    } else {
      throw new ActivitiIllegalArgumentException("'" + operation + "' is not a valid operation");
    }
    
    ObjectNode successNode = new ObjectMapper().createObjectNode();
    successNode.put("success", true);
    return successNode;
  }
}

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

import java.util.List;

import org.activiti.engine.form.FormProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TaskPropertiesResource extends SecuredResource {
  
  @Get
  public ObjectNode getTaskProperties() {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    List<FormProperty> properties = ActivitiUtil.getFormService().getTaskFormData(taskId).getFormProperties();
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    
    ArrayNode propertiesJSON = new ObjectMapper().createArrayNode();
    
    for (FormProperty property : properties) {
      ObjectNode propertyJSON = new ObjectMapper().createObjectNode();
      propertyJSON.put("id", property.getId());
      propertyJSON.put("name", property.getName());
      propertyJSON.put("value", property.getValue());
      propertyJSON.put("type", property.getType().getName());
      propertyJSON.put("required", property.isRequired());
      propertyJSON.put("readable", property.isReadable());
      propertyJSON.put("writable", property.isWritable());

      propertiesJSON.add(propertyJSON);
    }
  
    responseJSON.put("data", propertiesJSON);
    
    return responseJSON;
  }

}

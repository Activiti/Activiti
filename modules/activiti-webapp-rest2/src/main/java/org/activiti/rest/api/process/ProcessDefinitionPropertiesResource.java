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

package org.activiti.rest.api.process;

import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionPropertiesResource extends SecuredResource {
  
  @Get
  public ObjectNode getStartFormProperties() {
    if(authenticate() == false) return null;
    
    String processDefinitionId = (String) getRequest().getAttributes().get("processDefinitionId");
    StartFormData startFormData = ActivitiUtil.getFormService().getStartFormData(processDefinitionId);
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    
    ArrayNode propertiesJSON = new ObjectMapper().createArrayNode();
    
    if(startFormData != null) {
    
      List<FormProperty> properties = startFormData.getFormProperties();
      
      for (FormProperty property : properties) {
        ObjectNode propertyJSON = new ObjectMapper().createObjectNode();
        propertyJSON.put("id", property.getId());
        propertyJSON.put("name", property.getName());
        
        if(property.getValue() != null) {
          propertyJSON.put("value", property.getValue());
        } else {
          propertyJSON.put("value", "null");
        }
        
        if(property.getType() != null) {
          propertyJSON.put("type", property.getType().getName());
          
          if(property.getType() instanceof EnumFormType) {
            @SuppressWarnings("unchecked")
            Map<String, String> valuesMap = (Map<String, String>) property.getType().getInformation("values");
            if(valuesMap != null) {
              ArrayNode valuesArray = new ObjectMapper().createArrayNode();
              propertyJSON.put("enumValues", valuesArray);
              
              for (String key : valuesMap.keySet()) {
                ObjectNode valueJSON = new ObjectMapper().createObjectNode();
                valueJSON.put("id", key);
                valueJSON.put("name", valuesMap.get(key));
                valuesArray.add(valueJSON);
              }
            }
          }
          
        } else {
          propertyJSON.put("type", "String");
        }
        
        propertyJSON.put("required", property.isRequired());
        propertyJSON.put("readable", property.isReadable());
        propertyJSON.put("writable", property.isWritable());
  
        propertiesJSON.add(propertyJSON);
      }
    }
  
    responseJSON.put("data", propertiesJSON);
    return responseJSON;
  }
}

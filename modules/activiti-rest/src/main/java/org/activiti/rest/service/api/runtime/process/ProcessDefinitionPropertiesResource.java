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

import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.form.EnumFormType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
@RestController
public class ProcessDefinitionPropertiesResource {
  
  @Autowired
  protected FormService formService;
  
  @Autowired
  protected ObjectMapper objectMapper;
  
  @RequestMapping(value="/process-definition/{processDefinitionId}/properties", method = RequestMethod.GET, produces="application/json")
  public ObjectNode getStartFormProperties(@PathVariable String processDefinitionId) {
    StartFormData startFormData = formService.getStartFormData(processDefinitionId);
    
    ObjectNode responseJSON = objectMapper.createObjectNode();
    
    ArrayNode propertiesJSON = objectMapper.createArrayNode();
    
    if(startFormData != null) {
    
      List<FormProperty> properties = startFormData.getFormProperties();
      
      for (FormProperty property : properties) {
        ObjectNode propertyJSON = objectMapper.createObjectNode();
        propertyJSON.put("id", property.getId());
        propertyJSON.put("name", property.getName());
        
        if (property.getValue() != null) {
          propertyJSON.put("value", property.getValue());
        } else {
          propertyJSON.putNull("value");
        }
        
        if(property.getType() != null) {
          propertyJSON.put("type", property.getType().getName());
          
          if (property.getType() instanceof EnumFormType) {
            @SuppressWarnings("unchecked")
            Map<String, String> valuesMap = (Map<String, String>) property.getType().getInformation("values");
            if (valuesMap != null) {
              ArrayNode valuesArray = objectMapper.createArrayNode();
              propertyJSON.put("enumValues", valuesArray);
              
              for (String key : valuesMap.keySet()) {
                ObjectNode valueJSON = objectMapper.createObjectNode();
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

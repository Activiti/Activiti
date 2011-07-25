package org.activiti.rest.api.task;

import java.util.List;

import org.activiti.engine.form.FormProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

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

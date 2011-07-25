package org.activiti.rest.api.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

public class TaskOperationResource extends SecuredResource {
  
  @Put
  public void executeTaskOperation(Representation entity) {
    if(authenticate() == false) return;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    String operation = (String) getRequest().getAttributes().get("operation");
    try {
      String startParams = entity.getText();
      JsonNode startJSON = new ObjectMapper().readTree(startParams);
      Iterator<String> itName = startJSON.getFieldNames();
      Map<String, Object> variables = new HashMap<String, Object>();
      while(itName.hasNext()) {
        String name = itName.next();
        JsonNode valueNode = startJSON.path(name);
        if("true".equals(valueNode.getTextValue()) || "false".equals(valueNode.getTextValue())) {
          variables.put(name, Boolean.valueOf(valueNode.getTextValue()));
        } else {
          variables.put(name, valueNode.getTextValue());
        }
      }
      String currentUserId = "kermit";
      if ("claim".equals(operation)) {
        ActivitiUtil.getTaskService().claim(taskId, currentUserId);
      } else if ("complete".equals(operation)) {
        variables.remove("taskId");
        ActivitiUtil.getTaskService().complete(taskId, variables);
      } else {
        throw new ActivitiException("'" + operation + "' is not a valid operation");
      }
      
    } catch(Exception e) {
      throw new ActivitiException("Did not receive the operation parameters", e);
    }
  }
}

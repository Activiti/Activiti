package org.activiti.rest.api.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class ProcessInstanceResource extends SecuredResource {
  
  @Post
  public ProcessInstanceResponse startProcessInstance(Representation entity) {
    try {
      if(authenticate() == false) return null;
      
      String startParams = entity.getText();
      JsonNode startJSON = new ObjectMapper().readTree(startParams);
      String processDefinitionKey = startJSON.path("processDefinitionKey").getTextValue();
      String processDefinitionId = null;
      if (processDefinitionKey == null) {
        processDefinitionId = startJSON.path("processDefinitionId").getTextValue();
      }
      JsonNode businessKeyJson = startJSON.path("businessKey");
      String businessKey = null;
      if(businessKeyJson != null) {
        businessKey = businessKeyJson.getTextValue();
      }
      
      Map<String, Object> variables = new HashMap<String, Object>();
      Iterator<String> itName = startJSON.getFieldNames();
      while(itName.hasNext()) {
        String name = itName.next();
        variables.put(name, startJSON.path(name).getTextValue()); 
      }
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
      ProcessInstanceResponse response = new ProcessInstanceResponse(processInstance);
      return response;
      
    } catch (Exception e) {
      throw new ActivitiException("Failed to retrieve the process definition parameters", e);
    }
  }
}

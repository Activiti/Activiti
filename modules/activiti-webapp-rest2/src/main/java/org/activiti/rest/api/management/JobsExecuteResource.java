package org.activiti.rest.api.management;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class JobsExecuteResource extends SecuredResource {
  
  @Post
  public ObjectNode startProcessInstance(Representation entity) {
    try {
      if(authenticate(SecuredResource.ADMIN) == false) return null;
      
      String startParams = entity.getText();
      JsonNode startJSON = new ObjectMapper().readTree(startParams);
      ArrayNode jobIdsJSON = (ArrayNode) startJSON.get("jobIds");
      for (JsonNode jobId : jobIdsJSON) {
        ActivitiUtil.getManagementService().executeJob(jobId.getTextValue());
      }
      
      ObjectNode successNode = new ObjectMapper().createObjectNode();
      successNode.put("success", true);
      return successNode;
      
    } catch (Exception e) {
      throw new ActivitiException("Failed to execute jobs", e);
    }
  }
}

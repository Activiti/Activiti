package org.activiti.rest.api.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class DeploymentsDeleteResource extends SecuredResource {
  
  @Post
  public ObjectNode deleteDeployments(Representation entity) {
    try {
      if(authenticate(SecuredResource.ADMIN) == false) return null;
      Boolean cascade = RequestUtil.getBoolean(getQuery(), "cascade", false);
      String startParams = entity.getText();
      JsonNode idJSON = new ObjectMapper().readTree(startParams);
      ArrayNode idArray = (ArrayNode) idJSON.get("deploymentIds");
      for (JsonNode deploymentId : idArray) {
        if (cascade) {
          ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId.getTextValue(), true);
        }
        else {
          ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId.getTextValue());
        }
      }
      ObjectNode successNode = new ObjectMapper().createObjectNode();
      successNode.put("success", true);
      return successNode;
    } catch(Exception e) {
      throw new ActivitiException("Failed to delete deployments", e);
    }
  }
}

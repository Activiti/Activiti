package org.activiti.rest.api.repository;

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Delete;

public class DeploymentDeleteResource extends SecuredResource {
  
  @Delete
  public ObjectNode deleteDeployment() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    String deploymentId = (String) getRequest().getAttributes().get("deploymentId");
    Boolean cascade = RequestUtil.getBoolean(getQuery(), "cascade", false);
    if (cascade) {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId, true);
    }
    else {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId);
    }
    ObjectNode successNode = new ObjectMapper().createObjectNode();
    successNode.put("success", true);
    return successNode;
  }
}

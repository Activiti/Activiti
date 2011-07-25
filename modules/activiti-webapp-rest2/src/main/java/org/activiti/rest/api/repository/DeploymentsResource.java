package org.activiti.rest.api.repository;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class DeploymentsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public DeploymentsResource() {
    properties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
    properties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
    properties.put("deploymentTime", DeploymentQueryProperty.DEPLOY_TIME);
  }
  
  @Get
  public DataResponse getDeployments() {
    if(authenticate() == false) return null;
    
    DataResponse response = new DeploymentsPaginateList().paginateList(getQuery(), 
        ActivitiUtil.getRepositoryService().createDeploymentQuery(), "id", properties);
    return response;
  }
}

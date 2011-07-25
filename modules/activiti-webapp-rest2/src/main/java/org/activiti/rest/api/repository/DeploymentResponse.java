package org.activiti.rest.api.repository;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.api.RequestUtil;

public class DeploymentResponse {

  String id;
  String name;
  String deploymentTime;
  
  public DeploymentResponse(Deployment deployment) {
    setId(deployment.getId());
    setName(deployment.getName());
    setDeploymentTime(RequestUtil.dateToString(deployment.getDeploymentTime()));
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDeploymentTime() {
    return deploymentTime;
  }
  public void setDeploymentTime(String deploymentTime) {
    this.deploymentTime = deploymentTime;
  }
}

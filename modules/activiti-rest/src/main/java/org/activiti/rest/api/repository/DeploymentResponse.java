package org.activiti.rest.api.repository;

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

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.api.RequestUtil;

/**
 * @author Tijs Rademakers
 * @author Frederik Heremans
 */
public class DeploymentResponse {

  String id;
  String name;
  String deploymentTime;
  String category;
  String url;
  
  public DeploymentResponse(Deployment deployment, String url) {
    setId(deployment.getId());
    setName(deployment.getName());
    setDeploymentTime(RequestUtil.dateToString(deployment.getDeploymentTime()));
    setCategory(deployment.getCategory());
    setUrl(url);
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
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUrl() {
    return url;
  }
}

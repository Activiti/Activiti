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

package org.activiti.rest.service.api.repository;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.RequestUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class DeploymentResource extends SecuredResource {
  
  @Get
  public DeploymentResponse getDeployment() {
    if(authenticate() == false) return null;

    String deploymentId = getAttribute("deploymentId");
    if(deploymentId == null) {
      throw new ActivitiIllegalArgumentException("The deploymentId cannot be null");
    }
    
    Deployment deployment = ActivitiUtil.getRepositoryService().createDeploymentQuery()
            .deploymentId(deploymentId)
            .singleResult();
    
    if(deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
      .createDeploymentResponse(this, deployment);
  }
  
  @Delete
  public void deleteDeployment() {
    if(authenticate() == false) return;
    
    String deploymentId = getAttribute("deploymentId");
    
    Boolean cascade = RequestUtil.getBoolean(getQuery(), "cascade", false);
    if (cascade) {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId, true);
    }
    else {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId);
    }
    getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

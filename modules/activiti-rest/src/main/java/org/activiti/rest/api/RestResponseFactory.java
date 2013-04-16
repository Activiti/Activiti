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

package org.activiti.rest.api;

import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.repository.DeploymentResourceResponse;
import org.activiti.rest.api.repository.ProcessDefinitionResponse;
import org.activiti.rest.api.repository.DeploymentResourceResponse.DeploymentResourceType;
import org.activiti.rest.api.repository.DeploymentResponse;
import org.restlet.data.MediaType;


/**
 * Default implementation of a {@link RestResponseFactory}.
 * 
 * @author Frederik Heremans
 */
public class RestResponseFactory {
  
  public DeploymentResponse createDeploymentResponse(SecuredResource resourceContext, Deployment deployment) {
    return new DeploymentResponse(deployment, resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, deployment.getId()));
  }
  
  public DeploymentResourceResponse createDeploymentResourceResponse(SecuredResource resourceContext, String deploymentId, String resourceId) {
    // Create URL's
    String resourceUrl = resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, deploymentId, resourceId);
    String resourceContentUrl = resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deploymentId, resourceId);
    
    // Fetch media-type
    MediaType mediaType = resourceContext.resolveMediaType(resourceId);
    String mediaTypeString = (mediaType != null) ? mediaType.toString() : null;
    
    // Determine type
    // TODO: do based on the returned resource-POJO from the API once ready instead of doing it here
    DeploymentResourceType type = DeploymentResourceType.RESOURCE;
    for(String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
      if(resourceId.endsWith(suffix)) {
        type = DeploymentResourceType.PROCESS_DEFINITION;
        break;
      }
    }
    return new DeploymentResourceResponse(resourceId, resourceUrl, resourceContentUrl, mediaTypeString, type);
  }
  
  public ProcessDefinitionResponse createProcessDefinitionResponse(SecuredResource resourceContext, ProcessDefinition processDefinition) {
    ProcessDefinitionResponse response = new ProcessDefinitionResponse();
    response.setUrl(resourceContext.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
    response.setId(processDefinition.getId());
    response.setKey(processDefinition.getKey());
    response.setVersion(processDefinition.getVersion());
    response.setCategory(processDefinition.getCategory());
    response.setName(processDefinition.getName());
    response.setDescription(processDefinition.getDescription());
    response.setSuspended(processDefinition.isSuspended());
    response.setStartFormDefined(processDefinition.hasStartFormKey());
    
    // Check if graphical notation defined
    // TODO: this method does an additional check to see if the process-definition exists which causes an additional query on top
    // of the one we already did to retrieve the processdefinition in the first place.
    ProcessDefinition deployedDefinition = ActivitiUtil.getRepositoryService().getProcessDefinition(processDefinition.getId());
    response.setGraphicalNotationDefined(((ProcessDefinitionEntity) deployedDefinition).isGraphicalNotationDefined());
    
    // Links to other resources
    response.setDeployment(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId()));
    response.setResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, processDefinition.getDeploymentId(), processDefinition.getResourceName()));
    if(processDefinition.getDiagramResourceName() != null) {
      response.setDiagramResource(resourceContext.createFullResourceUrl(RestUrls.URL_DEPLOYMENT_RESOURCE,
              processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName()));
    }
    return response;
  }
}

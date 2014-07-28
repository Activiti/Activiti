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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;


/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionIdentityLinkCollectionResource extends BaseProcessDefinitionResource {

  @Get
  public List<RestIdentityLink> getIdentityLinks() {
    if(!authenticate())
      return null;
    
    List<RestIdentityLink> result = new ArrayList<RestIdentityLink>();
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    
    List<IdentityLink> identityLinks = ActivitiUtil.getRepositoryService().getIdentityLinksForProcessDefinition(processDefinition.getId());
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    for(IdentityLink link : identityLinks) {
      result.add(responseFactory.createRestIdentityLink(this, link));
    }
    return result;
  }
  
  @Post
  public RestIdentityLink createIdentityLink(RestIdentityLink identityLink) {
    if(!authenticate())
      return null;
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    
    if(identityLink.getGroup() == null && identityLink.getUser() == null) {
      throw new ActivitiIllegalArgumentException("A group or a user is required to create an identity link.");
    }
    
    if(identityLink.getGroup() != null && identityLink.getUser() != null) {
      throw new ActivitiIllegalArgumentException("Only one of user or group can be used to create an identity link.");
    }
    
    if(identityLink.getGroup() != null) {
      ActivitiUtil.getRepositoryService().addCandidateStarterGroup(processDefinition.getId(), identityLink.getGroup());
    } else {
      ActivitiUtil.getRepositoryService().addCandidateStarterUser(processDefinition.getId(), identityLink.getUser());
    }
    
    // Always candidate for process-definition. User-provided value is ignored
    identityLink.setType(IdentityLinkType.CANDIDATE);
    
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestIdentityLink(this, identityLink.getType(), identityLink.getUser(), identityLink.getGroup(), null, processDefinition.getId(), null);
  }
  
}

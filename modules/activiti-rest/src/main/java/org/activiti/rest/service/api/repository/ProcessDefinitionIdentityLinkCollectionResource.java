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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessDefinitionIdentityLinkCollectionResource extends BaseProcessDefinitionResource {

  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/identitylinks", method = RequestMethod.GET, produces = "application/json")
  public List<RestIdentityLink> getIdentityLinks(@PathVariable String processDefinitionId, HttpServletRequest request) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    return restResponseFactory.createRestIdentityLinks(repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId()));
  }
  
  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/identitylinks", method = RequestMethod.POST, produces = "application/json")
  public RestIdentityLink createIdentityLink(@PathVariable String processDefinitionId, @RequestBody RestIdentityLink identityLink, 
      HttpServletRequest request, HttpServletResponse response) {
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    
    if (identityLink.getGroup() == null && identityLink.getUser() == null) {
      throw new ActivitiIllegalArgumentException("A group or a user is required to create an identity link.");
    }
    
    if (identityLink.getGroup() != null && identityLink.getUser() != null) {
      throw new ActivitiIllegalArgumentException("Only one of user or group can be used to create an identity link.");
    }
    
    if (identityLink.getGroup() != null) {
      repositoryService.addCandidateStarterGroup(processDefinition.getId(), identityLink.getGroup());
    } else {
      repositoryService.addCandidateStarterUser(processDefinition.getId(), identityLink.getUser());
    }
    
    // Always candidate for process-definition. User-provided value is ignored
    identityLink.setType(IdentityLinkType.CANDIDATE);
    
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createRestIdentityLink(identityLink.getType(), identityLink.getUser(), identityLink.getGroup(), null, 
        processDefinition.getId(), null);
  }
  
}

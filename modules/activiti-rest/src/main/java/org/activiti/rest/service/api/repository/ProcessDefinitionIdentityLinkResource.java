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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessDefinitionIdentityLinkResource extends BaseProcessDefinitionResource {

  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/identitylinks/{family}/{identityId}", method = RequestMethod.GET, produces = "application/json")
  public RestIdentityLink getIdentityLink(@PathVariable("processDefinitionId") String processDefinitionId, @PathVariable("family") String family, 
      @PathVariable("identityId") String identityId, HttpServletRequest request) {
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);

    validateIdentityLinkArguments(family, identityId);

    // Check if identitylink to get exists
    IdentityLink link = getIdentityLink(family, identityId, processDefinition.getId());
    
    return restResponseFactory.createRestIdentityLink(link);
  }

  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/identitylinks/{family}/{identityId}", method = RequestMethod.DELETE)
  public void deleteIdentityLink(@PathVariable("processDefinitionId") String processDefinitionId, @PathVariable("family") String family, 
      @PathVariable("identityId") String identityId, HttpServletResponse response) {
    
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);

    validateIdentityLinkArguments(family, identityId);

    // Check if identitylink to delete exists
    IdentityLink link = getIdentityLink(family, identityId, processDefinition.getId());
    if (link.getUserId() != null) {
      repositoryService.deleteCandidateStarterUser(processDefinition.getId(), link.getUserId());
    } else {
      repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), link.getGroupId());
    }
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  protected void validateIdentityLinkArguments(String family, String identityId) {
    if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family) && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
      throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
    }
    if (identityId == null) {
      throw new ActivitiIllegalArgumentException("IdentityId is required.");
    }
  }

  protected IdentityLink getIdentityLink(String family, String identityId, String processDefinitionId) {
    boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);

    // Perhaps it would be better to offer getting a single identitylink from
    // the API
    List<IdentityLink> allLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinitionId);
    for (IdentityLink link : allLinks) {
      boolean rightIdentity = false;
      if (isUser) {
        rightIdentity = identityId.equals(link.getUserId());
      } else {
        rightIdentity = identityId.equals(link.getGroupId());
      }

      if (rightIdentity && link.getType().equals(IdentityLinkType.CANDIDATE)) {
        return link;
      }
    }
    throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
  }
}

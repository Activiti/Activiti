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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionIdentityLinkResource extends BaseProcessDefinitionResource {

  @Get
  public RestIdentityLink getIdentityLink() {
    if (!authenticate())
      return null;

    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();

    // Extract and validate identity link from URL
    String family = getAttribute("family");
    String identityId = getAttribute("identityId");
    validateIdentityLinkArguments(family, identityId);

    // Check if identitylink to get exists
    IdentityLink link = getIdentityLink(family, identityId, processDefinition.getId());
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createRestIdentityLink(this, link);
  }

  @Delete
  public void deleteIdentityLink() {
    if (!authenticate())
      return;

    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();

    // Extract and validate identity link from URL
    String family = getAttribute("family");
    String identityId = getAttribute("identityId");
    validateIdentityLinkArguments(family, identityId);

    // Check if identitylink to delete exists
    IdentityLink link = getIdentityLink(family, identityId, processDefinition.getId());
    if(link.getUserId() != null) {
      ActivitiUtil.getRepositoryService().deleteCandidateStarterUser(processDefinition.getId(), link.getUserId());
    } else {
      ActivitiUtil.getRepositoryService().deleteCandidateStarterGroup(processDefinition.getId(), link.getGroupId());
    }
    
    setStatus(Status.SUCCESS_NO_CONTENT);
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
    List<IdentityLink> allLinks = ActivitiUtil.getRepositoryService().getIdentityLinksForProcessDefinition(processDefinitionId);
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

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

package org.activiti.rest.service.api.runtime.task;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskIdentityLinkFamilyResource extends TaskBaseResource {

  @Get
  public List<RestIdentityLink> getIdentityLinksForFamily() {
    if(!authenticate())
      return null;
    
    Task task = getTaskFromRequest();

    // Extract and validate identity link from URL
    String family = getAttribute("family");
    
    if(family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family)
            && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
      throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
    }
    
    boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);
    List<RestIdentityLink> results = new ArrayList<RestIdentityLink>();
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    
    List<IdentityLink> allLinks = ActivitiUtil.getTaskService().getIdentityLinksForTask(task.getId());
    for(IdentityLink link : allLinks) {
      boolean match = false;
      if(isUser) {
        match = link.getUserId() != null;
      } else {
        match = link.getGroupId() != null;
      }
      
      if(match) {
        results.add(responseFactory.createRestIdentityLink(this, link));
      }
    }
    return results;
  }
  
  @Delete
  public void deleteIdentityLink() {
  	if(!authenticate()) { return; }
  	
    Task task = getTaskFromRequest();

    // Extract and validate identity link from URL
    String family = getAttribute("family");
    String identityId = getAttribute("identityId");
    String type = getAttribute("type");
    validateIdentityLinkArguments(family, identityId, type);
    
    // Check if identitylink to delete exists
    getIdentityLink(family, identityId, type, task.getId());
    
    if(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family)) {
      ActivitiUtil.getTaskService().deleteUserIdentityLink(task.getId(), identityId, type);
    } else {
      ActivitiUtil.getTaskService().deleteGroupIdentityLink(task.getId(), identityId, type);
    }
    
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  protected void validateIdentityLinkArguments(String family, String identityId, String type) {
   
    if(identityId == null) {
      throw new ActivitiIllegalArgumentException("IdentityId is required.");
    }
    if(type == null) {
      throw new ActivitiIllegalArgumentException("Type is required.");
    }
  }
  
  protected IdentityLink getIdentityLink(String family, String identityId, String type, String taskId) {
    boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);
    
    // Perhaps it would be better to offer getting a single identitylink from the API
    List<IdentityLink> allLinks = ActivitiUtil.getTaskService().getIdentityLinksForTask(taskId);
    for(IdentityLink link : allLinks) {
      boolean rightIdentity = false;
      if(isUser) {
        rightIdentity = identityId.equals(link.getUserId());
      } else {
        rightIdentity = identityId.equals(link.getGroupId());
      }
      
      if(rightIdentity && link.getType().equals(type)) {
        return link;
      }
    }
    throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
  }
}

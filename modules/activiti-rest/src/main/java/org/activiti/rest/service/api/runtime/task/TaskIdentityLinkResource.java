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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
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
public class TaskIdentityLinkResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}", method = RequestMethod.GET, produces="application/json")
  public RestIdentityLink getIdentityLink(@PathVariable("taskId") String taskId, 
      @PathVariable("family") String family, @PathVariable("identityId") String identityId, 
      @PathVariable("type") String type, HttpServletRequest request) {
    
    Task task = getTaskFromRequest(taskId);
    validateIdentityLinkArguments(family, identityId, type);
    
    IdentityLink link = getIdentityLink(family, identityId, type, task.getId());
    
    return restResponseFactory.createRestIdentityLink(link);
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/identitylinks/{family}/{identityId}/{type}", method = RequestMethod.DELETE)
  public void deleteIdentityLink(@PathVariable("taskId") String taskId, 
      @PathVariable("family") String family, @PathVariable("identityId") String identityId, 
      @PathVariable("type") String type, HttpServletResponse response) {
    
    Task task = getTaskFromRequest(taskId);

    validateIdentityLinkArguments(family, identityId, type);
    
    // Check if identitylink to delete exists
    getIdentityLink(family, identityId, type, task.getId());
    
    if (RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family)) {
      taskService.deleteUserIdentityLink(task.getId(), identityId, type);
    } else {
      taskService.deleteGroupIdentityLink(task.getId(), identityId, type);
    }
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  protected void validateIdentityLinkArguments(String family, String identityId, String type) {
    if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family)
            && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
      throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
    }
    if (identityId == null) {
      throw new ActivitiIllegalArgumentException("IdentityId is required.");
    }
    if (type == null) {
      throw new ActivitiIllegalArgumentException("Type is required.");
    }
  }
  
  protected IdentityLink getIdentityLink(String family, String identityId, String type, String taskId) {
    boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);
    
    // Perhaps it would be better to offer getting a single identitylink from the API
    List<IdentityLink> allLinks = taskService.getIdentityLinksForTask(taskId);
    for (IdentityLink link : allLinks) {
      boolean rightIdentity = false;
      if (isUser) {
        rightIdentity = identityId.equals(link.getUserId());
      } else {
        rightIdentity = identityId.equals(link.getGroupId());
      }
      
      if (rightIdentity && link.getType().equals(type)) {
        return link;
      }
    }
    throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
  }
}

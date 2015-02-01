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

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.RestUrls;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskIdentityLinkFamilyResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/identitylinks/{family}", method = RequestMethod.GET, produces="application/json")
  public List<RestIdentityLink> getIdentityLinksForFamily(@PathVariable("taskId") String taskId, 
      @PathVariable("family") String family, HttpServletRequest request) {
    
    Task task = getTaskFromRequest(taskId);

    if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family)
            && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
      throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
    }
    
    boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);
    List<RestIdentityLink> results = new ArrayList<RestIdentityLink>();
    
    List<IdentityLink> allLinks = taskService.getIdentityLinksForTask(task.getId());
    for (IdentityLink link : allLinks) {
      boolean match = false;
      if (isUser) {
        match = link.getUserId() != null;
      } else {
        match = link.getGroupId() != null;
      }
      
      if (match) {
        results.add(restResponseFactory.createRestIdentityLink(link));
      }
    }
    return results;
  }
}

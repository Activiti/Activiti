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
import org.activiti.engine.task.Task;
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
public class TaskIdentityLinkCollectionResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/identitylinks", method = RequestMethod.GET, produces="application/json")
  public List<RestIdentityLink> getIdentityLinks(@PathVariable("taskId") String taskId, HttpServletRequest request) {
    Task task = getTaskFromRequest(taskId);
    return restResponseFactory.createRestIdentityLinks(taskService.getIdentityLinksForTask(task.getId()));
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/identitylinks", method = RequestMethod.POST, produces="application/json")
  public RestIdentityLink createIdentityLink(@PathVariable("taskId") String taskId, 
      @RequestBody RestIdentityLink identityLink, HttpServletRequest request, HttpServletResponse response) {
    
    Task task = getTaskFromRequest(taskId);
    
    if (identityLink.getGroup() == null && identityLink.getUser() == null) {
      throw new ActivitiIllegalArgumentException("A group or a user is required to create an identity link.");
    }
    
    if (identityLink.getGroup() != null && identityLink.getUser() != null) {
      throw new ActivitiIllegalArgumentException("Only one of user or group can be used to create an identity link.");
    }
    
    if (identityLink.getType() == null) {
      throw new ActivitiIllegalArgumentException("The identity link type is required.");
    }

    if (identityLink.getGroup() != null) {
      taskService.addGroupIdentityLink(task.getId(), identityLink.getGroup(), identityLink.getType());
    } else {
      taskService.addUserIdentityLink(task.getId(), identityLink.getUser(), identityLink.getType());
    }
    
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createRestIdentityLink(identityLink.getType(), identityLink.getUser(), 
        identityLink.getGroup(), task.getId(), null, null);
  }
}

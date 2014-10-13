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

package org.activiti.rest.service.api.identity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Group;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class GroupMembershipResource extends BaseGroupResource {

  @RequestMapping(value="/identity/groups/{groupId}/members/{userId}", method = RequestMethod.DELETE)
  public void deleteMembership(@PathVariable("groupId") String groupId, @PathVariable("userId") String userId,
      HttpServletRequest request, HttpServletResponse response) {
    
    Group group = getGroupFromRequest(groupId);
   
    // Check if user is not a member of group since API doesn't return typed exception
    if (identityService.createUserQuery()
        .memberOfGroup(group.getId())
        .userId(userId)
        .count() != 1) {
      
      throw new ActivitiObjectNotFoundException("User '" + userId + "' is not part of group '" + group.getId() + "'.", null);
    }
   
    identityService.deleteMembership(userId, group.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
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
public class GroupResource extends BaseGroupResource {

  @RequestMapping(value="/identity/groups/{groupId}", method = RequestMethod.GET, produces = "application/json")
  public GroupResponse getGroup(@PathVariable String groupId, HttpServletRequest request) {
    return restResponseFactory.createGroupResponse(getGroupFromRequest(groupId));
  }
  
  @RequestMapping(value="/identity/groups/{groupId}", method = RequestMethod.PUT, produces = "application/json")
  public GroupResponse updateGroup(@PathVariable String groupId, @RequestBody GroupRequest groupRequest, HttpServletRequest request) {
    Group group = getGroupFromRequest(groupId);

    if (groupRequest.getId() == null || groupRequest.getId().equals(group.getId())) {
      if (groupRequest.isNameChanged()) {
        group.setName(groupRequest.getName());
      }
      if (groupRequest.isTypeChanged()) {
        group.setType(groupRequest.getType());
      }
      identityService.saveGroup(group);
    } else {
      throw new ActivitiIllegalArgumentException("Key provided in request body doesn't match the key in the resource URL.");
    }
    
    return restResponseFactory.createGroupResponse(group);
  }
  
  @RequestMapping(value="/identity/groups/{groupId}", method = RequestMethod.DELETE)
  public void deleteGroup(@PathVariable String groupId, HttpServletResponse response) {
  	Group group = getGroupFromRequest(groupId);
    identityService.deleteGroup(group.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
@Api(tags = { "Groups" }, description = "Manage Groups", authorizations = { @Authorization(value = "basicAuth") })
public class GroupMembershipResource extends BaseGroupResource {

  @ApiOperation(value = "Delete a member from a group", tags = {"Groups"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the group was found and the member has been deleted. The response body is left empty intentionally."),
      @ApiResponse(code = 404, message = "Indicates the requested group was not found or that the user is not a member of the group. The status description contains additional information about the error.")
  })
  @RequestMapping(value = "/identity/groups/{groupId}/members/{userId}", method = RequestMethod.DELETE)
  public void deleteMembership(@ApiParam(name = "groupId", value="The id of the group to remove a member from.") @PathVariable("groupId") String groupId,@ApiParam(name = "userId", value="The id of the user to remove.") @PathVariable("userId") String userId, HttpServletRequest request, HttpServletResponse response) {

    Group group = getGroupFromRequest(groupId);

    // Check if user is not a member of group since API doesn't return typed
    // exception
    if (identityService.createUserQuery().memberOfGroup(group.getId()).userId(userId).count() != 1) {

      throw new ActivitiObjectNotFoundException("User '" + userId + "' is not part of group '" + group.getId() + "'.", null);
    }

    identityService.deleteMembership(userId, group.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

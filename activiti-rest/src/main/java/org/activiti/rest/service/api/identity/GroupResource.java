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
@Api(tags = { "Groups" }, description = "Manage Groups", authorizations = { @Authorization(value = "basicAuth") })
public class GroupResource extends BaseGroupResource {


  @ApiOperation(value = "Get a single group", tags = {"Groups"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the group exists and is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested group does not exist.")
  })
  @RequestMapping(value = "/identity/groups/{groupId}", method = RequestMethod.GET, produces = "application/json")
  public GroupResponse getGroup(@ApiParam(name="groupId", value="The id of the group to get.") @PathVariable String groupId, HttpServletRequest request) {
    return restResponseFactory.createGroupResponse(getGroupFromRequest(groupId));
  }


  @ApiOperation(value = "Update a group", tags = {"Groups"},
      notes = "All request values are optional. For example, you can only include the name attribute in the request body JSON-object, only updating the name of the group, leaving all other fields unaffected. When an attribute is explicitly included and is set to null, the group-value will be updated to null.")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the group was updated."),
      @ApiResponse(code = 404, message = "Indicates the requested group was not found."),
      @ApiResponse(code = 409, message = "Indicates the requested group was updated simultaneously.")
  })
  @RequestMapping(value = "/identity/groups/{groupId}", method = RequestMethod.PUT, produces = "application/json")
  public GroupResponse updateGroup(@ApiParam(name="groupId") @PathVariable String groupId, @RequestBody GroupRequest groupRequest, HttpServletRequest request) {
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

  @ApiOperation(value = "Delete a group", tags = {"Groups"})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the group was found and  has been deleted. Response-body is intentionally empty."),
      @ApiResponse(code = 404, message = "Indicates the requested group does not exist.")
  })
  @RequestMapping(value = "/identity/groups/{groupId}", method = RequestMethod.DELETE)
  public void deleteGroup(@ApiParam(name="groupId", value="The id of the group to delete.") @PathVariable String groupId, HttpServletResponse response) {
    Group group = getGroupFromRequest(groupId);
    identityService.deleteGroup(group.getId());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

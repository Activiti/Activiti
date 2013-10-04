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

package org.activiti.rest.service.api.legacy.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Ernesto Revilla
 */
public class LegacyGroupUsersResource extends SecuredResource {

  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

  public LegacyGroupUsersResource() {
    properties.put("id", UserQueryProperty.USER_ID);
    properties.put("firstName", UserQueryProperty.FIRST_NAME);
    properties.put("lastName", UserQueryProperty.LAST_NAME);
    properties.put("email", UserQueryProperty.EMAIL);
  }

  @Get
  public DataResponse getGroups() {
    if (authenticate() == false)
      return null;

    String groupId = (String) getRequest().getAttributes().get("groupId");
    if (groupId == null) {
      throw new ActivitiIllegalArgumentException("No groupId provided");
    }

    DataResponse dataResponse = new LegacyGroupUsersPaginateList().paginateList(
        getQuery(), ActivitiUtil.getIdentityService().createUserQuery()
            .memberOfGroup(groupId), "id", properties);
    return dataResponse;
  }

  @Post
  public LegacyStateResponse setUsers(ArrayList<String> userIds) {
    if (authenticate() == false)
      return null;
    String groupId = (String) getRequest().getAttributes().get("groupId");
    if (groupId == null) {
      throw new ActivitiIllegalArgumentException("No groupId provided");
    }
    if (userIds == null) {
      throw new ActivitiIllegalArgumentException("No userIds provided");
    }

    IdentityService identityService = ActivitiUtil.getIdentityService();
    // Check if user exists
    if (identityService.createGroupQuery().groupId(groupId).singleResult() == null)
      throw new ActivitiObjectNotFoundException("The user '" + groupId + "' does not exist.", User.class);

    // Check first if all users exist
    for (String userId : userIds) {
      if (identityService.createUserQuery().userId(userId).singleResult() == null)
        throw new ActivitiObjectNotFoundException("User '" + userId + " does not exist.", User.class);
    }
    for (String userId : userIds) {
      // Add only if not already member
      if (identityService.createUserQuery().userId(userId)
          .memberOfGroup(groupId).singleResult() == null)
        identityService.createMembership(userId, groupId);
    }
    return new LegacyStateResponse().setSuccess(true);
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }

}

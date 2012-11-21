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

package org.activiti.rest.api.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.DefaultPaginateList;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Tijs Rademakers
 */
public class UserGroupsResource extends SecuredResource {

  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

  public UserGroupsResource() {
    properties.put("id", GroupQueryProperty.GROUP_ID);
    properties.put("name", GroupQueryProperty.NAME);
    properties.put("type", GroupQueryProperty.TYPE);
  }

  @Get
  public DataResponse getGroups() {
    if (authenticate() == false)
      return null;

    String userId = (String) getRequest().getAttributes().get("userId");
    if (userId == null) {
      throw new ActivitiException("No userId provided");
    }

    DataResponse dataResponse = new DefaultPaginateList().paginateList(
        getQuery(), ActivitiUtil.getIdentityService().createGroupQuery()
            .groupMember(userId), "id", properties);
    return dataResponse;
  }

  @Post
  public StateResponse setGroups(ArrayList<String> groupIds) {
    if (authenticate() == false)
      return null;
    String userId = (String) getRequest().getAttributes().get("userId");
    if (userId == null) {
      throw new ActivitiException("No userId provided");
    }
    if (groupIds == null) {
      throw new ActivitiException("No groupIds provided");
    }

    IdentityService identityService = ActivitiUtil.getIdentityService();
    // Check if user exists
    if (identityService.createUserQuery().userId(userId).singleResult() == null)
      throw new ActivitiException("The user '" + userId + " does not exist.");

    // Check first if all groups exist
    for (String groupId : groupIds) {
      if (identityService.createGroupQuery().groupId(groupId).singleResult() == null)
        throw new ActivitiException("Group '" + groupId + "' does not exist.");
    }
    for (String groupId : groupIds) {
      // Add only if not already member
      if (identityService.createUserQuery().userId(userId)
          .memberOfGroup(groupId).singleResult() == null)
        identityService.createMembership(userId, groupId);
    }
    return new StateResponse().setSuccess(true);
  }

}

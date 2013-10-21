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

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.resource.Delete;
import org.restlet.data.Status;

/**
 * @author Ernesto Revilla
 */
public class LegacyUserGroupsDeleteResource extends SecuredResource {

  @Delete
  public LegacyStateResponse deleteGroup() {
    if (authenticate() == false)
      return null;
    String userId = (String) getRequest().getAttributes().get("userId");
    String groupId = (String) getRequest().getAttributes().get("groupId");
    if (userId == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No userId provided.");
      return new LegacyStateResponse().setSuccess(false);
    }
    
    if (groupId == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No groupId provided.");
      return new LegacyStateResponse().setSuccess(false);
    }

    IdentityService identityService = ActivitiUtil.getIdentityService();
    // Check if user exists
    if (identityService.createUserQuery().userId(userId).singleResult() == null) {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND, "The user '" + userId
          + "' does not exist.");
      return new LegacyStateResponse().setSuccess(false);
    }

    // Add only if not already member
    Group group = identityService.createGroupQuery().groupMember(userId)
        .groupId(groupId).singleResult();
    if (group != null)
      identityService.deleteMembership(userId, groupId);
    return new LegacyStateResponse().setSuccess(true);
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
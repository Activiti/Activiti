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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class GroupMembershipResource extends BaseGroupResource {

 @Delete
 public void deleteMembership() {
   Group group = getGroupFromRequest();
   
   String userId = getAttribute("userId");
   if(userId == null) {
     throw new ActivitiIllegalArgumentException("UserId cannot be null.");
   }
   
   // Check if user is not a member of group since API doesn't return typed exception
   if(ActivitiUtil.getIdentityService().createUserQuery()
     .memberOfGroup(group.getId())
     .userId(userId)
     .count() != 1) {
     throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND.getCode(), "User '" + userId + 
             "' is not part of group '" + group.getId() + "'.", null, null);
   }
   
   ActivitiUtil.getIdentityService().deleteMembership(userId, group.getId());
   setStatus(Status.SUCCESS_NO_CONTENT);
 }
}

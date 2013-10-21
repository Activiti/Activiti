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
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class GroupMembershipCollectionResource extends BaseGroupResource {

 @Post
 public MembershipResponse createMembership(MembershipRequest memberShip) {
   Group group = getGroupFromRequest();
   
   if(memberShip.getUserId() == null) {
     throw new ActivitiIllegalArgumentException("UserId cannot be null.");
   }
   
   // Check if user is member of group since API doesn't return typed exception
   if(ActivitiUtil.getIdentityService().createUserQuery()
     .memberOfGroup(group.getId())
     .userId(memberShip.getUserId())
     .count() > 0) {
     throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "User '" + memberShip.getUserId() + 
             "' is already part of group '" + group.getId() + "'.", null, null);
   }
   
   ActivitiUtil.getIdentityService().createMembership(memberShip.getUserId(), group.getId());
   setStatus(Status.SUCCESS_CREATED);
   
   return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
           .createMembershipResponse(this, memberShip.getUserId(), group.getId());
 }
}

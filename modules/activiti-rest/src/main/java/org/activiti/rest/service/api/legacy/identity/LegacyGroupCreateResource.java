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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * @author Ernesto Revilla
 */
public class LegacyGroupCreateResource extends SecuredResource {

  @Put()
  public LegacyStateResponse createGroup(LegacyGroupInfo groupInfo) {
    if (authenticate() == false)
      return null;

    IdentityService identityService = ActivitiUtil.getIdentityService();
    if (groupInfo == null || groupInfo.getId() == null) {
      throw new ActivitiIllegalArgumentException("No group id supplied");
    }
    if (groupInfo.getName() == null || groupInfo.getName().equals(""))
      groupInfo.setName(groupInfo.getId());

    if (identityService.createGroupQuery().groupId(groupInfo.getId()).count() == 0) {
      Group group = identityService.newGroup(groupInfo.getId());
      group.setName(groupInfo.getName());
      if (groupInfo.getType() != null) {
        group.setType(groupInfo.getType());
      } else {
        group.setType("assignment");
      }
      identityService.saveGroup(group);
    } else {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "group id must be unique");
    }
    return new LegacyStateResponse().setSuccess(true);
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}

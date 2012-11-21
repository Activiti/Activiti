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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class GroupResource extends SecuredResource {

  @Get
  public Group getGroup() {
    if (authenticate() == false)
      return null;

    String groupId = (String) getRequest().getAttributes().get("groupId");
    if (groupId == null) {
      throw new ActivitiException("No groupId provided");
    }
    Group group = ActivitiUtil.getIdentityService().createGroupQuery()
        .groupId(groupId).singleResult();
    return group;
  }

  @Delete
  public StateResponse deleteGroup() {
    if (authenticate() == false)
      return null;

    String groupId = (String) getRequest().getAttributes().get("groupId");
    if (groupId == null) {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND, "The group '" + groupId
          + "' does not exist.");
      return new StateResponse().setSuccess(false);
    }
    Group group = ActivitiUtil.getIdentityService().createGroupQuery()
        .groupId(groupId).singleResult();
    if (group != null) {
      ActivitiUtil.getIdentityService().deleteGroup(groupId);
      return new StateResponse().setSuccess(true);
    }
    return new StateResponse().setSuccess(false);
  }
}

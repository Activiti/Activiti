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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;

/**
 * @author Frederik Heremans
 */
public class BaseGroupResource extends SecuredResource {

  protected Group getGroupFromRequest() {
    String groupId = getAttribute("groupId");
    if (groupId == null) {
      throw new ActivitiIllegalArgumentException("The groupId cannot be null");
    }

    Group group = ActivitiUtil.getIdentityService().createGroupQuery().groupId(groupId).singleResult();

    if (group == null) {
      throw new ActivitiObjectNotFoundException("Could not find a group with id '" + groupId + "'.", User.class);
    }
    return group;
  }
}

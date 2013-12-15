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
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * @author Frederik Heremans
 */
public class GroupResource extends BaseGroupResource {

  @Get
  public GroupResponse getUser() {
    if(!authenticate())
      return null;
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createGroupResponse(this, getGroupFromRequest());
  }
  
  @Put
  public GroupResponse updateGroup(GroupRequest request) {

    Group group = getGroupFromRequest();

    if(request.getId() == null || request.getId().equals(group.getId())) {
      if(request.isNameChanged()) {
        group.setName(request.getName());
      }
      if(request.isTypeChanged()) {
        group.setType(request.getType());
      }
      ActivitiUtil.getIdentityService().saveGroup(group);
    } else {
      throw new ActivitiIllegalArgumentException("Key provided in request body doesn't match the key in the resource URL.");
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createGroupResponse(this, group);
  }
  
  @Delete
  public void deleteGroup() {
    Group group = getGroupFromRequest();
    ActivitiUtil.getIdentityService().deleteGroup(group.getId());
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

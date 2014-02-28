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

import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * @author Frederik Heremans
 */
public class UserResource extends BaseUserResource {

  @Get
  public UserResponse getUser() {
    if(!authenticate())
      return null;
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserResponse(this, getUserFromRequest(), false);
  }
  
  @Put
  public UserResponse updateUser(UserRequest request) {
  	if(authenticate() == false) return null;
  	
    User user = getUserFromRequest();
    if(request.isEmailChanged()) {
      user.setEmail(request.getEmail());
    }
    if(request.isFirstNameChanged()) {
      user.setFirstName(request.getFirstName());
    }
    if(request.isLastNameChanged()) {
      user.setLastName(request.getLastName());
    }
    if(request.isPasswordChanged()) {
      user.setPassword(request.getPassword());
    }
    
    ActivitiUtil.getIdentityService().saveUser(user);
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserResponse(this, user, false);
  }
  
  @Delete
  public void deleteUser() {
  	if(authenticate() == false) return;
  	
    User user = getUserFromRequest();
    ActivitiUtil.getIdentityService().deleteUser(user.getId());
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

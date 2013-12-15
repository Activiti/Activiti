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
public class UserInfoResource extends BaseUserResource {

  @Get
  public UserInfoResponse getUserInfo() {
    if(!authenticate())
      return null;
    
    User user = getUserFromRequest();
    
    String key = getAttribute("key");
    if(key == null) {
      throw new ActivitiIllegalArgumentException("Key cannot be null.");
    }
    
    String existingValue = ActivitiUtil.getIdentityService().getUserInfo(user.getId(), key);
    if(existingValue == null) {
      throw new ActivitiObjectNotFoundException("User info with key '" + key + "' does not exists for user '" + user.getId() + "'.", null);
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserInfoResponse(this, key, existingValue, user.getId());
  }
  
  
  @Put
  public UserInfoResponse setUserInfo(UserInfoRequest request) {
    if(!authenticate())
      return null;
    
    User user = getUserFromRequest();
    String key = getValidKeyFromRequest(user);
    
    if(request.getValue() == null) {
      throw new ActivitiIllegalArgumentException("The value cannot be null.");
    }
    
    if(request.getKey() == null || key.equals(request.getKey())) {
      ActivitiUtil.getIdentityService().setUserInfo(user.getId(), key, request.getValue());
    } else {
      throw new ActivitiIllegalArgumentException("Key provided in request body doesn't match the key in the resource URL.");
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserInfoResponse(this, key, request.getValue(), user.getId());
  }
  
  @Delete
  public void deleteUserInfo() {
    User user = getUserFromRequest();
    String key = getValidKeyFromRequest(user);
    
    ActivitiUtil.getIdentityService().setUserInfo(user.getId(), key, null);
    
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  protected String getValidKeyFromRequest(User user) {
    String key = getAttribute("key");
    if(key == null) {
      throw new ActivitiIllegalArgumentException("Key cannot be null.");
    }
    
    String existingValue = ActivitiUtil.getIdentityService().getUserInfo(user.getId(), key);
    if(existingValue == null) {
      throw new ActivitiObjectNotFoundException("User info with key '" + key + "' does not exists for user '" + user.getId() + "'.", null);
    }
    
    return key;
  }
}

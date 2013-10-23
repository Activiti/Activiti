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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class UserInfoCollectionResource extends BaseUserResource {

  @Get
  public List<UserInfoResponse> getUserInfo() {
    if(!authenticate())
      return null;
    
    User user = getUserFromRequest();
    
    RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    List<UserInfoResponse> responses = new ArrayList<UserInfoResponse>();
    
    // Create responses for all keys,not including value as this is exposed through the individual resource URL.
    for(String key : ActivitiUtil.getIdentityService().getUserInfoKeys(user.getId())) {
      responses.add(factory.createUserInfoResponse(this, key, null, user.getId()));
    }
    
    return responses;
  }
  
  
  @Post
  public UserInfoResponse setUserInfo(UserInfoRequest request) {
    if(!authenticate())
      return null;
    
    User user = getUserFromRequest();
    
    if(request.getKey() == null) {
      throw new ActivitiIllegalArgumentException("The key cannot be null.");
    }
    if(request.getValue() == null) {
      throw new ActivitiIllegalArgumentException("The value cannot be null.");
    }
    
    String existingValue = ActivitiUtil.getIdentityService().getUserInfo(user.getId(), request.getKey());
    if(existingValue != null) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "User info with key '" + request.getKey() + "' already exists for this user.", null, null);
    }
    
    ActivitiUtil.getIdentityService().setUserInfo(user.getId(), request.getKey(), request.getValue());
    
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createUserInfoResponse(this, request.getKey(), request.getValue(), user.getId());
  }
}

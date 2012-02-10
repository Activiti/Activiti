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
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Put;

/**
 * @author Tijs Rademakers
 */
public class UserCreateResource extends SecuredResource {
  
  @Put()
  public StateResponse createUser(UserInfoWithPassword userInfo){
    if(authenticate() == false) return null;
    
    IdentityService identityService = ActivitiUtil.getIdentityService();
    if(userInfo == null || userInfo.getId() == null) {
      throw new ActivitiException("No user id supplied");
    }

    if (identityService.createUserQuery().userId(userInfo.getId()).count() == 0) {
       User user = identityService.newUser(userInfo.getId());
       user.setFirstName(userInfo.getFirstName());
       user.setLastName(userInfo.getLastName());
       user.setPassword(userInfo.getPassword());
       user.setEmail(userInfo.getEmail());
       identityService.saveUser(user);
    } else  {
      throw new ActivitiException("user id must be unique");
    }
    return new StateResponse().setSuccess(true);
  }
}

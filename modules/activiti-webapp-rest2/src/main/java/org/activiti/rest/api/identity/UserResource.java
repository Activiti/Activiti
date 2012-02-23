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
import org.activiti.engine.identity.User;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class UserResource extends SecuredResource {
  
  @Get
  public UserInfo getUser() {
    if(authenticate() == false) return null;
    
    String userId = (String) getRequest().getAttributes().get("userId");
    if(userId == null) {
      throw new ActivitiException("No userId provided");
    }
    User user = ActivitiUtil.getIdentityService().createUserQuery().userId(userId).singleResult();
    UserInfo response = new UserInfo(user);
    return response;
  }

}

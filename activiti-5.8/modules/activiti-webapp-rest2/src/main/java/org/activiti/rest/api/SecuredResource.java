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

package org.activiti.rest.api;

import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.rest.application.ActivitiRestApplication;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class SecuredResource extends ServerResource {

  protected static final String USER = "user";
  protected static final String ADMIN = "admin";
  
  protected String loggedInUser;
  
  protected boolean authenticate() {
    return authenticate(null);
  }
  
  protected boolean authenticate(String group) {
    loggedInUser = ((ActivitiRestApplication) getApplication()).authenticate(getRequest(), getResponse());
    if(loggedInUser == null) {
      // Not authenticated
      setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Authentication is required");
      return false;
    
    } else if(group == null) {
      return true;
    
    } else {
      boolean allowed = false;
      List<Group> groupList = ActivitiUtil.getIdentityService().createGroupQuery().groupMember(loggedInUser).list();
      if(groupList != null) {
        for (Group groupObject : groupList) {
          if(groupObject.getId().equals(group)) {
            allowed = true;
            break;
          }
        }
      }
      if(allowed == false) {
        setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User is not part of the group " + group);
      }
      return allowed;
    }
  }

}

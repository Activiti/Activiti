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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class LegacyUserSearchResource extends SecuredResource {
  
  @Get("json")
  public DataResponse searchUsers() {
    if(authenticate() == false) return null;
    
    String searchText = (String) getQuery().getValues("searchText");
    if(searchText == null) {
      throw new ActivitiIllegalArgumentException("No searchText provided");
    }
    searchText = "%" + searchText + "%";
    
    List<User> firstNameMatchList = ActivitiUtil.getIdentityService().createUserQuery().userFirstNameLike(searchText).list();
    List<User> lastNameMatchList = ActivitiUtil.getIdentityService().createUserQuery().userLastNameLike(searchText).list();
    
    Map<String, LegacyUserInfo> userMap = new HashMap<String, LegacyUserInfo>();
    if(firstNameMatchList != null) {
      for (User user : firstNameMatchList) {
        userMap.put(user.getId(), new LegacyUserInfo(user));
      }
    }
    
    if(lastNameMatchList != null) {
      for (User user : lastNameMatchList) {
        if(userMap.containsKey(user.getId()) == false) {
          userMap.put(user.getId(), new LegacyUserInfo(user));
        }
      }
    }
    
    List<LegacyUserInfo> userList = new ArrayList<LegacyUserInfo>();
    userList.addAll(userMap.values());
    Collections.sort(userList, new UserResponseComparable());
    
    DataResponse response = new DataResponse();
    response.setStart(0);
    response.setSize(userList.size()); 
    response.setSort("lastName");
    response.setOrder("asc");
    response.setTotal(userList.size());
    response.setData(userList);
    
    return response;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
  
  protected class UserResponseComparable implements Comparator<LegacyUserInfo>{
    
    public int compare(LegacyUserInfo user1, LegacyUserInfo user2) {
        return user1.getLastName().compareTo(user2.getLastName());
    }
  }

}

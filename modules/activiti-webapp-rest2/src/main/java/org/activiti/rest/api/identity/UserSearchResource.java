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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class UserSearchResource extends SecuredResource {
  
  @Get
  public DataResponse searchUsers() {
    if(authenticate() == false) return null;
    
    String searchText = (String) getRequest().getAttributes().get("searchText");
    if(searchText == null) {
      throw new ActivitiException("No searchText provided");
    }
    searchText = "%" + searchText + "%";
    
    List<User> firstNameMatchList = ActivitiUtil.getIdentityService().createUserQuery().userFirstNameLike(searchText).list();
    
    List<User> lastNameMatchList = ActivitiUtil.getIdentityService().createUserQuery().userLastNameLike(searchText).list();
    
    Map<String, UserInfo> userMap = new HashMap<String, UserInfo>();
    if(firstNameMatchList != null) {
      for (User user : firstNameMatchList) {
        userMap.put(user.getId(), new UserInfo(user));
      }
    }
    
    if(lastNameMatchList != null) {
      for (User user : lastNameMatchList) {
        if(userMap.containsKey(user.getId()) == false) {
          userMap.put(user.getId(), new UserInfo(user));
        }
      }
    }
    
    List<UserInfo> userList = new ArrayList<UserInfo>();
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
  
  protected class UserResponseComparable implements Comparator<UserInfo>{
    
    @Override
    public int compare(UserInfo user1, UserInfo user2) {
        return user1.getLastName().compareTo(user2.getLastName());
    }
  }

}

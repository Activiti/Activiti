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

package org.activiti.explorer.cache;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;



/**
 * TEMPORARY cache impl - must be reviewed !
 * 
 * TODO: cannot work with duplicated!
 * 
 * TODO: in cluster, must be refreshed every x minutes
 * 
 * @author Joram Barrez
 */
public class UserCache {
  
  // TODO: Move to ExplorerApp? Or maybe even Spring wired?
  protected static UserCache INSTANCE = new UserCache();
  
  protected IdentityService identityService;
  
  // TODO: evaluate if this is overkill ...
  protected RadixTree<List<UserDetails>> radixTree = new RadixTreeImpl<List<UserDetails>>();
  
  protected UserCache() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    loadUsers();
  }
  
  public void refresh() {
    radixTree = new RadixTreeImpl<List<UserDetails>>();
    loadUsers();
  }
  
  public void loadUsers() {
    List<User> users = identityService.createUserQuery().list();
    for (User user : users) {
      String firstName = user.getFirstName();
      String lastName = user.getLastName();
      
      if (firstName != null && !"".equals(firstName)) {
        for (String firstNameToken : user.getFirstName().split(" ")) {
          addCacheItem(firstNameToken, user);
        }
      }
      
      if (lastName != null && !"".equals(lastName)) {
        for (String lastNameToken : user.getLastName().split(" ")) {
          addCacheItem(lastNameToken, user);
        }
      }
      
    }
  }
  
  protected void addCacheItem(String key, User user) {
    key = key.toLowerCase();
    List<UserDetails> value = null;
    if (!radixTree.contains(key)) {
      value = new ArrayList<UserDetails>();
    } else {
      value = radixTree.find(key);
    }
    
    UserDetails userDetails = new UserDetails(user.getId(), 
            user.getFirstName().toLowerCase() + " " + user.getLastName().toLowerCase());
    
    value.add(userDetails);
    radixTree.delete(key);
    radixTree.insert(key, value);
  }
  
  public List<UserDetails> findMatchingUsers(String prefix) {
    List<UserDetails> returnValue = new ArrayList<UserDetails>();
    List<List<UserDetails>> results = radixTree.searchPrefix(prefix.toLowerCase(), 100);
    for (List<UserDetails> result : results) {
      for (UserDetails userDetail : result) {
        returnValue.add(userDetail);
      }
    }
    return returnValue;
  }
  
  public static UserCache getInstance() {
    return INSTANCE;
  }
  
  public class UserDetails {
    
    protected String userId;
    protected String fullName;
    
    public UserDetails(String userId, String fullName) {
      this.userId = userId;
      this.fullName = fullName;
    }

    public String getUserId() {
      return userId;
    }
    public void setUserId(String userId) {
      this.userId = userId;
    }
    public String getFullName() {
      return fullName;
    }
    public void setFullName(String fullName) {
      this.fullName = fullName;
    }
    
  }
  
}

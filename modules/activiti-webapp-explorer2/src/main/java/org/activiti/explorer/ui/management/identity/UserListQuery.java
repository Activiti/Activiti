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

package org.activiti.explorer.ui.management.identity;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Joram Barrez
 */
public class UserListQuery extends AbstractLazyLoadingQuery {
  
  protected IdentityService identityService;
  
  public UserListQuery() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  }

  public int size() {
    return (int) identityService.createUserQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<User> users = identityService.createUserQuery()
      .orderByUserFirstName().asc()
      .orderByUserLastName().asc()
      .orderByUserId().asc()
      .listPage(start, count);
    
    List<Item> userListItems = new ArrayList<Item>();
    for (User user : users) {
      userListItems.add(new UserListItem(user));
    }
    return userListItems;
  }

  public Item loadSingleResult(String id) {
    return new UserListItem(identityService.createUserQuery().userId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class UserListItem extends PropertysetItem implements Comparable<UserListItem> {
    
    private static final long serialVersionUID = 1L;

    public UserListItem(User user) {
      addItemProperty("id", new ObjectProperty<String>(user.getId(), String.class));
      addItemProperty("name", new ObjectProperty<String>(user.getFirstName() 
              + " " + user.getLastName() + " (" + user.getId() + ")", String.class));
    }

    public int compareTo(UserListItem other) {
      // Users are ordered by default by firstname + lastname, and then on id
      String name = (String) getItemProperty("name").getValue();
      String otherName = (String) other.getItemProperty("name").getValue();
      
      int comparison = name.compareTo(otherName);
      if (comparison != 0) {
        return comparison;
      } else {
        String id = (String) getItemProperty("id").getValue();
        String otherId = (String) other.getItemProperty("id").getValue();
        return id.compareTo(otherId);
      }
    }
    
  }

}

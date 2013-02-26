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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * Query that selects available groups for a user.
 * 
 * @author Joram Barrez
 */
public class GroupSelectionQuery extends AbstractLazyLoadingQuery {
  
  protected transient IdentityService identityService;
  protected String userId;
  
  public GroupSelectionQuery(IdentityService identityService, String userId) {
    this.identityService = identityService;
    this.userId = userId;
  }

  public int size() {
    return (int) (identityService.createGroupQuery().count() 
            - identityService.createGroupQuery().groupMember(userId).count());
  }

  public List<Item> loadItems(int start, int count) {
    List<Item> groupItems = new ArrayList<Item>();
    Set<String> currentGroups = getCurrentGroups();
    
    int nrFound = 0;
    int tries = 0;
    while (nrFound < count && tries < 5) { // must stop at some point in time, as otherwise size() would be reached
      
      List<Group> groups = identityService.createGroupQuery()
        .orderByGroupType().asc()
        .orderByGroupId().asc()
        .orderByGroupName().asc()
        .listPage(start + (tries * count), count);
      
      for (Group group : groups) {
        if (!currentGroups.contains(group.getId())) {
          nrFound++;
          groupItems.add(new GroupSelectionItem(group));
        } 
      }
      
      tries++;
    }
    
    return groupItems;
  }
  
  protected Set<String> getCurrentGroups() {
    Set<String> groupIds = new HashSet<String>();
    List<Group> currentGroups = identityService.createGroupQuery().groupMember(userId).list();
    for (Group group : currentGroups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  public Item loadSingleResult(String id) {
    throw new UnsupportedOperationException();
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class GroupSelectionItem extends PropertysetItem {
    
    private static final long serialVersionUID = 1L;

    public GroupSelectionItem(Group group) {
      addItemProperty("id", new ObjectProperty<String>(group.getId(), String.class));
      if (group.getName() != null) {
        addItemProperty("name", new ObjectProperty<String>(group.getName(), String.class));
      }
      if (group.getType() != null) {
        addItemProperty("type", new ObjectProperty<String>(group.getType(), String.class));
      }
    }
    
  }

}

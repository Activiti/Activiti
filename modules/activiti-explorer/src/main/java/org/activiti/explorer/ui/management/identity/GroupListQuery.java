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
import org.activiti.engine.identity.Group;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Joram Barrez
 */
public class GroupListQuery extends AbstractLazyLoadingQuery {
  
  protected transient IdentityService identityService;
  
  public GroupListQuery() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  }

  public int size() {
    return (int) identityService.createGroupQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<Group> groups = identityService.createGroupQuery()
      .orderByGroupId().asc()
      .orderByGroupName().asc()
      .listPage(start, count);
    
    List<Item> groupListItems = new ArrayList<Item>();
    for (Group group : groups) {
      groupListItems.add(new GroupListItem(group));
    }
    return groupListItems;
  }

  public Item loadSingleResult(String id) {
    return new GroupListItem(identityService.createGroupQuery().groupId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class GroupListItem extends PropertysetItem implements Comparable<GroupListItem> {
    
    private static final long serialVersionUID = 1L;
    
    public GroupListItem(Group group) {
      addItemProperty("id", new ObjectProperty<String>(group.getId(), String.class));
      if (group.getName() != null) {
        addItemProperty("name", new ObjectProperty<String>(group.getName()
                + " (" + group.getName() + ")", String.class));
      } else {
        addItemProperty("name", new ObjectProperty<String>("(" + group.getId() + ")", String.class));
      }
    }

    public int compareTo(GroupListItem other) {
      String id = (String) getItemProperty("id").getValue();
      String otherId = (String) other.getItemProperty("id").getValue();
      return id.compareTo(otherId);
    }
    
  }

}

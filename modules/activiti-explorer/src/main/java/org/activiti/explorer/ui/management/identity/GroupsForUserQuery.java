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
import org.activiti.engine.identity.Group;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class GroupsForUserQuery extends AbstractLazyLoadingQuery {
  
  protected transient IdentityService identityService;
  protected String userId;
  protected UserDetailPanel userDetailPanel;
  
  public GroupsForUserQuery(IdentityService identityService, UserDetailPanel userDetailPanel, String userId) {
    this.identityService = identityService;
    this.userDetailPanel = userDetailPanel;
    this.userId = userId;
  }

  public int size() {
    return (int) identityService.createGroupQuery().groupMember(userId).count();
  }

  public List<Item> loadItems(int start, int count) {
    List<Group> groups = identityService.createGroupQuery()
      .groupMember(userId)
      .orderByGroupType().asc()
      .orderByGroupId().asc()
      .orderByGroupName().asc()
      .list();
    
    List<Item> groupItems = new ArrayList<Item>();
    for (Group group : groups) {
      groupItems.add(new GroupItem(group));
    }
    return groupItems;
  }

  public Item loadSingleResult(String id) {
    throw new UnsupportedOperationException();
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class GroupItem extends PropertysetItem {
    
    private static final long serialVersionUID = 1L;

    public GroupItem(final Group group) {
      Button idButton = new Button(group.getId());
      idButton.addStyleName(Reindeer.BUTTON_LINK);
      idButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          ExplorerApp.get().getViewManager().showGroupPage(group.getId());
        }
      });
      addItemProperty("id", new ObjectProperty<Button>(idButton, Button.class));
      
      if (group.getName() != null) {
        addItemProperty("name", new ObjectProperty<String>(group.getName(), String.class));
      }
      if (group.getType() != null) {
        addItemProperty("type", new ObjectProperty<String>(group.getType(), String.class));
      }
      
      Embedded deleteIcon = new Embedded(null, Images.DELETE);
      deleteIcon.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
      deleteIcon.addListener(new DeleteMembershipListener(identityService, userId, group.getId(), userDetailPanel));
      addItemProperty("actions", new ObjectProperty<Embedded>(deleteIcon, Embedded.class));
    }
    
  }

}

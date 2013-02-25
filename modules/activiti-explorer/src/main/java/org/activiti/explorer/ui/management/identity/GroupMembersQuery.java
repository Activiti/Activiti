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
import org.activiti.engine.identity.UserQuery;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;


/**
 * {@link LazyLoadingQuery} that queries all members of a given group.
 * Used in the {@link GroupDetailPanel} to display all group members.
 * 
 * @author Joram Barrez
 */
public class GroupMembersQuery extends AbstractLazyLoadingQuery {
  
  protected transient IdentityService identityService;

  protected String groupId;
  protected MemberShipChangeListener memberShipChangeListener;
  protected String sortby;
  protected boolean ascending;
  
  public GroupMembersQuery(String groupId, MemberShipChangeListener memberShipChangeListener) {
    this.groupId = groupId;
    this.memberShipChangeListener = memberShipChangeListener;
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  }

  public int size() {
    return (int) identityService.createUserQuery().memberOfGroup(groupId).count();
  }

  public List<Item> loadItems(int start, int count) {
     UserQuery query = identityService.createUserQuery().memberOfGroup(groupId);
    
    if (sortby == null || "id".equals(sortby)) {
      query.orderByUserId(); // default
    } else if ("firstName".equals(sortby)){
      query.orderByUserFirstName();
    } else if ("lastName".equals(sortby)) {
      query.orderByUserLastName();
    } else if ("email".equals(sortby)) {
      query.orderByUserEmail();
    }
    
    if (sortby == null || ascending) {
      query.asc();
    } else {
      query.desc();
    }
    
    List<User> users = query.listPage(start, count);
    
    List<Item> items = new ArrayList<Item>();
    for (User user : users) {
      items.add(new GroupMemberItem(user));
    }
    return items;
  }

  public Item loadSingleResult(String id) {
    throw new UnsupportedOperationException();
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    if (propertyIds.length > 0) {
      this.sortby = propertyIds[0].toString();
      this.ascending = ascending[0];
    }
  }
  
  class GroupMemberItem extends PropertysetItem {
    
    private static final long serialVersionUID = 1L;

    public GroupMemberItem(final User user) {
      // id
      Button idButton = new Button(user.getId());
      idButton.addStyleName(Reindeer.BUTTON_LINK);
      idButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          ExplorerApp.get().getViewManager().showUserPage(user.getId());
        }
      });
      addItemProperty("id", new ObjectProperty<Button>(idButton, Button.class));
      
      // name
      if (user.getFirstName() != null) {
        addItemProperty("firstName", new ObjectProperty<String>(user.getFirstName(), String.class));
      }
      if (user.getLastName() != null) {
        addItemProperty("lastName", new ObjectProperty<String>(user.getLastName(), String.class));
      }
      
      // email
      if (user.getEmail() != null) {
        addItemProperty("email", new ObjectProperty<String>(user.getEmail(), String.class));
      }
      
      // Delete
      Embedded deleteIcon = new Embedded(null, Images.DELETE);
      deleteIcon.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
      deleteIcon.addListener(new DeleteMembershipListener(identityService, user.getId(), groupId, memberShipChangeListener));
      addItemProperty("actions", new ObjectProperty<Embedded>(deleteIcon, Embedded.class));
    }
    
  }

}

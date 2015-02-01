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

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.GroupNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;

/**
 * Page for managing groups.
 * 
 * @author Joram Barrez
 */
public class GroupPage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  protected String groupId;
  protected Table groupTable;
  protected LazyLoadingQuery groupListQuery;
  protected LazyLoadingContainer groupListContainer;
  
  public GroupPage() {
    ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(GroupNavigator.GROUP_URI_PART));
  }
  
  public GroupPage(String groupId) {
    this.groupId = groupId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if (groupId == null) {
      selectElement(0);
    } else {
      selectElement(groupListContainer.getIndexForObjectId(groupId));
    }
  }

  protected Table createList() {
    groupTable = new Table();
    
    groupTable.setEditable(false);
    groupTable.setImmediate(true);
    groupTable.setSelectable(true);
    groupTable.setNullSelectionAllowed(false);
    groupTable.setSortDisabled(true);
    groupTable.setSizeFull();
    
    groupListQuery = new GroupListQuery();
    groupListContainer = new LazyLoadingContainer(groupListQuery, 30);
    groupTable.setContainerDataSource(groupListContainer);
    
    // Column headers
    groupTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.GROUP_22));
    groupTable.setColumnWidth("icon", 22);
    groupTable.addContainerProperty("name", String.class, null);
    groupTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
            
    // Listener to change right panel when clicked on a user
    groupTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        Item item = groupTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String groupId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new GroupDetailPanel(GroupPage.this, groupId));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(GroupNavigator.GROUP_URI_PART, groupId));
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(GroupNavigator.GROUP_URI_PART, groupId));
        }
      }
    });
    
    return groupTable;
  }
  
  public void notifyGroupChanged(String groupId) {
    // Clear cache
    groupTable.removeAllItems();
    groupListContainer.removeAllItems();
    
    // select changed group
    groupTable.select(groupListContainer.getIndexForObjectId(groupId));
  }
  
}

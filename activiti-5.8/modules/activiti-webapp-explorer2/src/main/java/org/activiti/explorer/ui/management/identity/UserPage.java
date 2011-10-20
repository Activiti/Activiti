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
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.navigation.UserNavigator;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


/**
 * Page for managing users.
 * 
 * @author Joram Barrez
 */
public class UserPage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  protected String userId;
  protected Table userTable;
  protected LazyLoadingQuery userListQuery;
  protected LazyLoadingContainer userListContainer;
  
  public UserPage() {
    ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(UserNavigator.USER_URI_PART));
  }
  
  public UserPage(String userId) {
    this.userId = userId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if (userId == null) {
      selectElement(0);
    } else {
      selectElement(userListContainer.getIndexForObjectId(userId));
    }
  }

  protected Table createList() {
    userTable = new Table();
    
    userListQuery = new UserListQuery();
    userListContainer = new LazyLoadingContainer(userListQuery, 20);
    userTable.setContainerDataSource(userListContainer);
    
    // Column headers
    userTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.USER_22));
    userTable.setColumnWidth("icon", 22);
    userTable.addContainerProperty("name", String.class, null);
    userTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
            
    // Listener to change right panel when clicked on a user
    userTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        Item item = userTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String userId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new UserDetailPanel(UserPage.this, userId));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(UserNavigator.USER_URI_PART, userId));
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(UserNavigator.USER_URI_PART));
        }
      }
    });
    
    return userTable;
  }
  
  /**
   * Call when some user data has been changed
   */
  public void notifyUserChanged(String userId) {
    // Clear cache
    userTable.removeAllItems();
    userListContainer.removeAllItems();
    
    userTable.select(userListContainer.getIndexForObjectId(userId));
  }
  
}

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
package org.activiti.explorer.ui.management.admin;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.management.ManagementPage;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;

/**
 * @author Tijs Rademakers
 */
public class AdministrationPage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected String managementId;
  protected Table managementTable;
  
  public AdministrationPage() {
    ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(AdministrationNavigator.MANAGEMENT_URI_PART));
    this.i18nManager = ExplorerApp.get().getI18nManager();
  }
  
  public AdministrationPage(String managementId) {
    this.managementId = managementId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    int index = 0;
    if (managementId != null) {
      index = Integer.valueOf(managementId);
    }
    managementTable.select(index);
    managementTable.setCurrentPageFirstItemId(index);
  }

  protected Table createList() {
  	managementTable = new Table();
    
  	managementTable.setEditable(false);
  	managementTable.setImmediate(true);
  	managementTable.setSelectable(true);
  	managementTable.setNullSelectionAllowed(false);
  	managementTable.setSortDisabled(true);
  	managementTable.setSizeFull();
    
    // Column headers
    managementTable.addContainerProperty("name", String.class, null);
    managementTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    managementTable.addItem(new String[] {i18nManager.getMessage(Messages.ADMIN_MENU_RUNNING)}, 0);
    managementTable.addItem(new String[] {i18nManager.getMessage(Messages.ADMIN_MENU_COMPLETED)}, 1);
    managementTable.addItem(new String[] {i18nManager.getMessage(Messages.ADMIN_MENU_DATABASE)}, 2);
            
    // Listener to change right panel when clicked on a user
    managementTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        Item item = managementTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
        	
        	if("0".equals(event.getProperty().getValue().toString())) {
        		setDetailComponent(new AdminRunningInstancesPanel());
        	} else if("1".equals(event.getProperty().getValue().toString())) {
        		setDetailComponent(new AdminCompletedInstancesPanel());
        	} else if("2".equals(event.getProperty().getValue().toString())) {
        		setDetailComponent(new AdminDatabaseSettingsPanel());
        	}
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(AdministrationNavigator.MANAGEMENT_URI_PART, event.getProperty().getValue().toString()));
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(AdministrationNavigator.MANAGEMENT_URI_PART, managementId));
        }
      }
    });
    
    return managementTable;
  }
  
  public void notifyGroupChanged(String managementId) {
    // Clear cache
  	managementTable.removeAllItems();
    
    // select changed group
    managementTable.select(Integer.valueOf(managementId));
  }
  
}

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
package org.activiti.explorer.ui.management.db;

import java.util.TreeMap;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.navigation.DatabaseNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class DatabasePage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  
  protected ManagementService managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
  protected String tableName;
  
  public DatabasePage() {
    ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(DatabaseNavigator.TABLE_URI_PART));
  }
  
  public DatabasePage(String tableName) {
    this();
    this.tableName = tableName;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    populateTableList(); // tablelist is NOT lazy loaded, since there are only a few tables
    if (tableName == null) {
      selectElement(0);
    } else {
      table.select(tableName);
    }
  }
  
  @Override
  protected Table createList() {
    final Table tableList = new Table();
    
    // Listener to change right panel when clicked on a task
    tableList.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        // The itemId of the table list is the tableName
        String tableName = (String) event.getProperty().getValue();
        setDetailComponent(new DatabaseDetailPanel(tableName));
       
       // Update URL
       ExplorerApp.get().setCurrentUriFragment(
         new UriFragment(DatabaseNavigator.TABLE_URI_PART, tableName));
      }
    });
    
    // Create column headers
    tableList.addContainerProperty("icon", Embedded.class, null);
    tableList.setColumnWidth("icon", 22);
    tableList.addContainerProperty("tableName", String.class, null);
    tableList.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return tableList;
  }
  
  protected void populateTableList() {
    TreeMap<String, Long> tables = new TreeMap<String, Long>(managementService.getTableCount()); // treemap because we want to sort it on name
    for (String tableName : tables.keySet()) {
      Item item = table.addItem(tableName);
      item.getItemProperty("icon").setValue(determineTableIcon(tableName));
      item.getItemProperty("tableName").setValue(tableName + " (" + tables.get(tableName) + ")");
    }
  }
  
  protected Embedded determineTableIcon(String tableName) {
      Resource image = null;
      if (tableName.startsWith("ACT_HI")) {
        image = Images.DATABASE_HISTORY;
      } else if (tableName.startsWith("ACT_RU")) {
        image = Images.DATABASE_RUNTIME;
      } else if (tableName.startsWith("ACT_RE")) {
        image = Images.DATABASE_REPOSITORY;
      } else if (tableName.startsWith("ACT_ID")) {
        image = Images.DATABASE_IDENTITY;
      } else {
        image = Images.DATABASE_22;
      }
      return new Embedded(null, image);
    }

}

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
package org.activiti.explorer.ui.management;

import java.util.TreeMap;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class DatabasePage extends ManagementPage {

  private static final long serialVersionUID = -3989067128946859490L;
  
  // services
  protected ManagementService managementService;
  
  // ui
  protected Table tableList;
  
  public DatabasePage() {
    super();
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    
    addTableList();
    populateTableList();
  }
  
  protected void addTableList() {
    this.tableList = new Table();
    mainSplitPanel.setFirstComponent(tableList);
    
    // Set non-editable, selectable and full-size
    tableList.setEditable(false);
    tableList.setImmediate(true);
    tableList.setSelectable(true);
    tableList.setNullSelectionAllowed(false);
    tableList.setSizeFull();
            
    // Listener to change right panel when clicked on a task
    tableList.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        // The itemId of the table list is the tableName
       mainSplitPanel.setSecondComponent(new DatabaseDetailPanel((String) event.getProperty().getValue()));
      }
    });
    
    // Create column header
    tableList.addContainerProperty("tableName", String.class, null);
    tableList.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
  }
  
  protected void populateTableList() {
    TreeMap<String, Long> tables = new TreeMap<String, Long>(managementService.getTableCount()); // treemap because we want to sort it on name
    for (String tableName : tables.keySet()) {
      Item item = tableList.addItem(tableName);
      item.getItemProperty("tableName").setValue(tableName + " (" + tables.get(tableName) + ")");
    }
  }
  

}

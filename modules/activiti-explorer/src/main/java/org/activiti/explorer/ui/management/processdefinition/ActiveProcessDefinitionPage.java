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
package org.activiti.explorer.ui.management.processdefinition;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.ActiveProcessDefinitionNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.management.ManagementPage;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;

/**
 * @author Joram Barrez
 */
public class ActiveProcessDefinitionPage extends ManagementPage {
  
  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected Table processDefinitionTable;
  protected LazyLoadingQuery processDefinitionListQuery;
  protected LazyLoadingContainer processDefinitionListContainer;
  
  public ActiveProcessDefinitionPage() {
    ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(ActiveProcessDefinitionNavigator.ACTIVE_PROC_DEF_URI_PART));
  }
  
  public ActiveProcessDefinitionPage(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  protected void initUi() {
    super.initUi();
    
    if (processDefinitionId == null) {
      selectElement(0);
    } else {
      selectElement(processDefinitionListContainer.getIndexForObjectId(processDefinitionId));
    }
  }

  protected Table createList() {
    processDefinitionTable = new Table();

    processDefinitionListQuery = new ActiveProcessDefinitionListQuery();
    processDefinitionListContainer = new LazyLoadingContainer(processDefinitionListQuery);
    processDefinitionTable.setContainerDataSource(processDefinitionListContainer);
    
    // Column headers
    processDefinitionTable.addContainerProperty("name", String.class, null);
    processDefinitionTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
            
    // Listener to change right panel when clicked on a user
    processDefinitionTable.addListener(new Property.ValueChangeListener() {
      
      private static final long serialVersionUID = 1L;
      
      public void valueChange(ValueChangeEvent event) {
        Item item = processDefinitionTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if (item != null) {
          String processDefinitionId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new ActiveProcessDefinitionDetailPanel(processDefinitionId, ActiveProcessDefinitionPage.this));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
                  new UriFragment(ActiveProcessDefinitionNavigator.ACTIVE_PROC_DEF_URI_PART, processDefinitionId));
          
        } else {
          // Nothing selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(ActiveProcessDefinitionNavigator.ACTIVE_PROC_DEF_URI_PART));
        }
      }
      
    });
    
    return processDefinitionTable;
  }
  
}

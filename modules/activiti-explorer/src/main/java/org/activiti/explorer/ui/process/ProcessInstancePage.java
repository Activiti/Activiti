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

package org.activiti.explorer.ui.process;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.management.processinstance.ProcessInstanceDetailPanel;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Frederik Heremans
 */
public abstract class ProcessInstancePage extends AbstractTablePage {

  private static final long serialVersionUID = 1L;

  protected LazyLoadingContainer processInstanceListContainer;
  protected LazyLoadingQuery lazyLoadingQuery;
  
  @Override
  protected ToolBar createMenuBar() {
    return new ProcessMenuBar();
  }

  @Override
  protected Table createList() {
    final Table processInstanceTable = new Table();
    processInstanceTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_LIST);
    
    // Listener to change right panel when clicked on a process instance
    processInstanceTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = processInstanceTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String processInstanceId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new ProcessInstanceDetailPanel(processInstanceId, ProcessInstancePage.this));
          
          UriFragment taskFragment = getUriFragment(processInstanceId);
          ExplorerApp.get().setCurrentUriFragment(taskFragment);
        } else {
          // Nothing is selected
          setDetailComponent(null);
          UriFragment taskFragment = getUriFragment(null);
          ExplorerApp.get().setCurrentUriFragment(taskFragment);
        }
      }
    });
    
    this.lazyLoadingQuery = createLazyLoadingQuery();
    this.processInstanceListContainer = new LazyLoadingContainer(lazyLoadingQuery, 30);
    processInstanceTable.setContainerDataSource(processInstanceListContainer);
    
    // Create column header
    processInstanceTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS_22));
    processInstanceTable.setColumnWidth("icon", 22);
    
    processInstanceTable.addContainerProperty("name", String.class, null);
    processInstanceTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return processInstanceTable;
  }
  
  protected abstract LazyLoadingQuery createLazyLoadingQuery();
  
  protected abstract UriFragment getUriFragment(String processInstanceId);

}

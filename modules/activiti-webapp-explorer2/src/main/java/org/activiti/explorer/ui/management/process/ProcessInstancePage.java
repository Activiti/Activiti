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

package org.activiti.explorer.ui.management.process;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.DeploymentNavigator;
import org.activiti.explorer.navigation.ProcessInstanceNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class ProcessInstancePage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  
  protected LazyLoadingContainer processInstanceContainer;
  protected String processInstanceId; 
  
  public ProcessInstancePage() {
    ExplorerApp.get().setCurrentUriFragment(
      new UriFragment(DeploymentNavigator.DEPLOYMENT_URI_PART));
  }
  
  public ProcessInstancePage(String processInstanceId) {
    this();
    this.processInstanceId = processInstanceId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if (processInstanceId == null) {
      selectElement(0);
    } else {
      selectElement(processInstanceContainer.getIndexForObjectId(processInstanceId));
    }
  }
  
  protected Table createList() {
    final Table table = new Table();
    
    LazyLoadingQuery query = new ProcessInstanceListQuery();
    processInstanceContainer = new LazyLoadingContainer(query);
    table.setContainerDataSource(processInstanceContainer);
    
    table.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        Item item = table.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String processInstanceId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new AlfrescoProcessInstanceDetailPanel(processInstanceId, ProcessInstancePage.this));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(ProcessInstanceNavigator.PROCESS_INSTANCE_URL_PART, processInstanceId));
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(ProcessInstanceNavigator.PROCESS_INSTANCE_URL_PART));
        }
      }
    });
    
    // Create column headers
    table.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS_22));
    table.setColumnWidth("icon", 22);
    
    table.addContainerProperty("name", String.class, null);
    table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return table;
  }
  
}

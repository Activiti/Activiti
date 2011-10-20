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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.ProcessNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionPage extends AbstractTablePage {
  
  private static final long serialVersionUID = 1L;
  
  // Services
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  
  // UI
  protected String processDefinitionId;
  protected LazyLoadingContainer processDefinitionContainer;
  protected Table processDefinitionTable;
  protected ProcessDefinitionDetailPanel detailPanel;
  
  public ProcessDefinitionPage() {
    ExplorerApp.get().setCurrentUriFragment(
      new UriFragment(ProcessNavigator.process_URI_PART));
  }
  
  /**
   * Used when the page is reached through an URL.
   * The page will be built and the given process definition will be selected.
   */
  public ProcessDefinitionPage(String processDefinitionId) {
    this();
    this.processDefinitionId = processDefinitionId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    if (processDefinitionId == null) {
      selectElement(0);
    } else {
      selectElement(processDefinitionContainer.getIndexForObjectId(processDefinitionId));
    }
  }
  
  @Override
  protected ToolBar createMenuBar() {
   return new ProcessMenuBar();
  }
  
  @Override
  protected Table createList() {
    final Table processDefinitionTable = new Table();
    processDefinitionTable.addStyleName(ExplorerLayout.STYLE_PROCESS_DEFINITION_LIST);
    
    // Set non-editable, selectable and full-size
    processDefinitionTable.setEditable(false);
    processDefinitionTable.setImmediate(true);
    processDefinitionTable.setSelectable(true);
    processDefinitionTable.setNullSelectionAllowed(false);
    processDefinitionTable.setSortDisabled(true);
    processDefinitionTable.setSizeFull();
    
    
    LazyLoadingQuery lazyLoadingQuery = new ProcessDefinitionListQuery(repositoryService);
    this.processDefinitionContainer = new LazyLoadingContainer(lazyLoadingQuery, 10);
    processDefinitionTable.setContainerDataSource(processDefinitionContainer);
    
    // Listener to change right panel when clicked on a task
    processDefinitionTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;

      public void valueChange(ValueChangeEvent event) {
        Item item = processDefinitionTable.getItem(event.getProperty().getValue());
        String processDefinitionId = (String) item.getItemProperty("id").getValue();
        showProcessDefinitionDetail(processDefinitionId);
      }
    });
    
    // Create columns
    processDefinitionTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS_22));
    processDefinitionTable.setColumnWidth("icon", 22);
    
    processDefinitionTable.addContainerProperty("name", String.class, null);
    processDefinitionTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return processDefinitionTable;
  }

  protected void showProcessDefinitionDetail(String processDefinitionId) {
    detailPanel = new ProcessDefinitionDetailPanel(processDefinitionId, this);
    setDetailComponent(detailPanel);
    changeUrl(processDefinitionId);
  }

  protected void changeUrl(String processDefinitionId) {
    UriFragment processDefinitionFragment = new UriFragment(ProcessNavigator.process_URI_PART, processDefinitionId);
    ExplorerApp.get().setCurrentUriFragment(processDefinitionFragment);
  }
  
  public void showStartForm(ProcessDefinition processDefinition, StartFormData startFormData) {
    if(detailPanel != null) {
      showProcessDefinitionDetail(processDefinition.getId());
    }
    detailPanel.showProcessStartForm(startFormData);
  }
  
}

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

package org.activiti.editor.ui;

import java.util.List;

import org.activiti.editor.data.dao.ModelDao;
import org.activiti.editor.data.model.ModelData;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.navigation.ProcessModelNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.ProcessMenuBar;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Tijs Rademakers
 */
public class EditorProcessDefinitionPage extends AbstractTablePage {
  
  private static final long serialVersionUID = 1L;
  
  // Services
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  
  // UI
  protected String modelId;
  protected EditorProcessDefinitionDetailPanel detailPanel;
  
  public EditorProcessDefinitionPage() {
    ExplorerApp.get().setCurrentUriFragment(new UriFragment(ProcessModelNavigator.PROCESS_MODEL_URI_PART));
  }
  
  /**
   * Used when the page is reached through an URL.
   * The page will be built and the given process definition will be selected.
   */
  public EditorProcessDefinitionPage(String modelId) {
    this();
    this.modelId = modelId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    if (modelId == null) {
      table.select(table.firstItemId());
    } else {
      table.select(Long.valueOf(modelId));
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
    
    // Listener to change right panel when clicked on a model
    processDefinitionTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;

      public void valueChange(ValueChangeEvent event) {
        showProcessDefinitionDetail((Long) event.getProperty().getValue());
      }
    });
    
    // Create columns
    processDefinitionTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS_22));
    processDefinitionTable.setColumnWidth("icon", 22);
    
    processDefinitionTable.addContainerProperty("name", String.class, null);
    processDefinitionTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    List<ModelData> modelList = new ModelDao().getModels();
    for (ModelData modelData : modelList) {
      Item item = processDefinitionTable.addItem(modelData.getObjectId());
      item.getItemProperty("name").setValue(modelData.getName());
    }
    
    
    return processDefinitionTable;
  }

  protected void showProcessDefinitionDetail(Long selectedModelId) {
    detailPanel = new EditorProcessDefinitionDetailPanel(selectedModelId, this);
    setDetailComponent(detailPanel);
    changeUrl("" + selectedModelId);
  }

  protected void changeUrl(String processDefinitionId) {
    UriFragment processDefinitionFragment = new UriFragment(ProcessModelNavigator.PROCESS_MODEL_URI_PART, processDefinitionId);
    ExplorerApp.get().setCurrentUriFragment(processDefinitionFragment);
  }
}

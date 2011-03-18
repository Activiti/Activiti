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

package org.activiti.explorer.ui.flow;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.explorer.Constants;
import org.activiti.explorer.Images;
import org.activiti.explorer.ui.ThemeImageColumnGenerator;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class FlowPage extends VerticalLayout {
  
  private static final long serialVersionUID = 2310017323549425167L;
  
  // Services
  protected RepositoryService repositoryService;
  
  // UI
  protected VerticalLayout mainLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  protected Table processDefinitionTable;
  
  public FlowPage() {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    
    setSizeFull();
    
    initFlowMenuBar();
    initMainSplitPanel();
    initProcessDefinitionList();
  }
  
  protected void initProcessDefinitionList() {
    this.processDefinitionTable = new Table();
    processDefinitionTable.addStyleName(Constants.STYLE_PROCESS_DEFINITION_LIST);
    
    // Set non-editable, selectable and full-size
    processDefinitionTable.setEditable(false);
    processDefinitionTable.setImmediate(true);
    processDefinitionTable.setSelectable(true);
    processDefinitionTable.setNullSelectionAllowed(false);
    processDefinitionTable.setSizeFull();
    
    // Set table container to populate list with tasks
    BeanQueryFactory<ProcessDefinitionListQuery> queryFactory = 
        new BeanQueryFactory<ProcessDefinitionListQuery>(ProcessDefinitionListQuery.class);
    
    Map<String,Object> queryConfiguration = new HashMap<String,Object>();
    queryConfiguration.put("repositoryService", repositoryService);
    queryConfiguration.put("processDefinitionTable", processDefinitionTable);
    queryFactory.setQueryConfiguration(queryConfiguration);

    LazyQueryContainer container = new LazyQueryContainer(queryFactory, false, 10);
    processDefinitionTable.setContainerDataSource(container);
    
    // Listener to change right panel when clicked on a task
    processDefinitionTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8378856103392135871L;

      public void valueChange(ValueChangeEvent event) {
        Item item = processDefinitionTable.getItem(event.getProperty().getValue());
        String processDefinitionId = (String) item.getItemProperty("id").getValue();
        mainSplitPanel.setSecondComponent(new ProcessDefinitionDetailPanel(processDefinitionId));
      }
    });
    
    // Create columns
    processDefinitionTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS));
    processDefinitionTable.setColumnWidth("icon", 32);
    
    processDefinitionTable.addContainerProperty("name", String.class, null);
    processDefinitionTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    
    mainSplitPanel.setFirstComponent(processDefinitionTable);
  }

  protected void initMainSplitPanel() {
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(17, HorizontalSplitPanel.UNITS_PERCENTAGE);
    addComponent(mainSplitPanel);
    setExpandRatio(mainSplitPanel, 1.0f);
  }

  protected void initFlowMenuBar() {
    FlowMenuBar flowMenuBar = new FlowMenuBar();
    addComponent(flowMenuBar);
  }

}

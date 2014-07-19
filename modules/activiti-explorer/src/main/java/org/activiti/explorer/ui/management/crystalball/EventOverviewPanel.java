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
package org.activiti.explorer.ui.management.crystalball;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.variable.VariableRendererManager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Tijs Rademakers
 */
public class EventOverviewPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected transient HistoryService historyService;
  protected transient RepositoryService repositoryService;
  protected transient RuntimeService runtimeService;
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;
  protected VariableRendererManager variableRendererManager;

  protected HorizontalLayout instanceLayout;
  protected NativeSelect definitionSelect;
  protected Button replayButton;
  protected Table instanceTable;
  protected HorizontalLayout eventLayout;
  protected Table eventTable;
  protected Label noMembersTable;
  
  protected List<ProcessDefinition> definitionList;
  protected Map<String, ProcessDefinition> definitionMap = new HashMap<String, ProcessDefinition>();
  protected List<HistoricProcessInstance> instanceList;
  
  public EventOverviewPanel() {
  	this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.variableRendererManager = ExplorerApp.get().getVariableRendererManager();
    this.definitionList = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().list();
    this.instanceList = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initializeDefinitionMap();
    init();
  }
  
  protected void initializeDefinitionMap() {
    for (ProcessDefinition definition : definitionList) {
      definitionMap.put(definition.getId(), definition);
    }
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initProcessInstances();
    initEvents();
  }
  
  protected void initProcessInstances() {
    HorizontalLayout selectLayout = new HorizontalLayout();
    selectLayout.setSpacing(true);
    selectLayout.setMargin(true);
    selectLayout.setWidth(50, UNITS_PERCENTAGE);
    addDetailComponent(selectLayout);
    
    definitionSelect = new NativeSelect(i18nManager.getMessage(Messages.DEPLOYMENT_HEADER_DEFINITIONS));
    definitionSelect.setImmediate(true);
    for (ProcessDefinition definition : definitionList) {
      definitionSelect.addItem(definition.getId());
      definitionSelect.setItemCaption(definition.getId(), definition.getName());
    }
    definitionSelect.addListener(new ValueChangeListener() {
      
      private static final long serialVersionUID = 1L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        if (definitionSelect.getValue() != null) {
          refreshInstances((String) definitionSelect.getValue());
        }
      }
    });
    
    selectLayout.addComponent(definitionSelect);
    
    replayButton = new Button("Replay");
    replayButton.setEnabled(false);
    replayButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        
      }
    });
    selectLayout.addComponent(replayButton);
    selectLayout.setComponentAlignment(replayButton, Alignment.MIDDLE_LEFT);
    
    instanceLayout = new HorizontalLayout();
    instanceLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(instanceLayout);
    
    initInstancesTable();
  }
  
  protected void initProcessInstanceTitle(HorizontalLayout instancesHeader) {
    Label titleHeader = new Label(i18nManager.getMessage(Messages.ADMIN_DEFINITIONS));
    titleHeader.addStyleName(ExplorerLayout.STYLE_H3);
    instancesHeader.addComponent(titleHeader);
  }
  
  protected void initInstancesTable() {
    if (instanceList == null || instanceList.size() == 0) {
      noMembersTable = new Label(i18nManager.getMessage(Messages.ADMIN_RUNNING_NONE_FOUND));
      instanceLayout.addComponent(noMembersTable);
    
    } else {
      
      instanceTable = new Table();
      instanceTable.setWidth(100, UNITS_PERCENTAGE);
      instanceTable.setHeight(200, UNITS_PIXELS);
      
      instanceTable.setEditable(false);
      instanceTable.setImmediate(true);
      instanceTable.setSelectable(true);
      instanceTable.setSortDisabled(false);
      
      instanceTable.addContainerProperty("id", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ID), null, Table.ALIGN_LEFT);
      instanceTable.addContainerProperty("definitionName", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_NAME), null, Table.ALIGN_LEFT);
      instanceTable.addContainerProperty("started", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_STARTED), null, Table.ALIGN_LEFT);
      instanceTable.addContainerProperty("ended", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_STARTED), null, Table.ALIGN_LEFT);
      
      fillInstanceValues();
      
      instanceTable.addListener(new Property.ValueChangeListener() {
        private static final long serialVersionUID = 1L;
        public void valueChange(ValueChangeEvent event) {
          Item item = instanceTable.getItem(event.getProperty().getValue());
          if (item != null) {
            replayButton.setEnabled(true);
          } else {
            replayButton.setEnabled(false);
          }
        }
      });
      
      instanceLayout.addComponent(instanceTable);
    } 
  }
  
  protected void refreshInstances(String processDefinitionId) {
    instanceList = historyService.createHistoricProcessInstanceQuery()
        .processDefinitionId(processDefinitionId)
        .orderByProcessInstanceStartTime()
        .desc()
        .list();
    instanceTable.removeAllItems();
    fillInstanceValues();
  }
  
  protected void fillInstanceValues() {
    for (HistoricProcessInstance processInstance : instanceList) {
      ProcessDefinition definition = definitionMap.get(processInstance.getProcessDefinitionId());
      String definitionName = "";
      if (definition != null) {
        if (definition.getName() != null) {
          definitionName = definition.getName();
        } else {
          definitionName = definition.getId();
        }
        
        definitionName += " (v" + definition.getVersion() + ")";
      }
      
      instanceTable.addItem(new String[]{ processInstance.getId(), 
          definitionName, processInstance.getStartTime().toString(), 
          processInstance.getEndTime() != null ? processInstance.getEndTime().toString() : ""}, processInstance.getId());
    }
  }
  
  protected void initEvents() {
    HorizontalLayout eventsHeader = new HorizontalLayout();
    eventsHeader.setSpacing(true);
    eventsHeader.setWidth(100, UNITS_PERCENTAGE);
    eventsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(eventsHeader);
    
    initEventTitle(eventsHeader);
    
    eventLayout = new HorizontalLayout();
    eventLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(eventLayout);
    initEventsTable();
  }
  
  protected void initEventTitle(HorizontalLayout eventsHeader) {
    Label usersHeader = new Label(i18nManager.getMessage(Messages.ADMIN_DEFINITIONS));
    usersHeader.addStyleName(ExplorerLayout.STYLE_H3);
    eventsHeader.addComponent(usersHeader);
  }
  
  protected void initEventsTable() {
    eventTable = new Table();
    eventTable.setWidth(100, UNITS_PERCENTAGE);
    eventTable.setHeight(250, UNITS_PIXELS);
    
    eventTable.setEditable(false);
    eventTable.setImmediate(true);
    eventTable.setSelectable(true);
    eventTable.setSortDisabled(false);
    
    eventTable.addContainerProperty("id", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ID), null, Table.ALIGN_LEFT);
    eventTable.addContainerProperty("name", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_NAME), null, Table.ALIGN_LEFT);
    eventTable.addContainerProperty("nr of instances", String.class, null, i18nManager.getMessage(Messages.ADMIN_NR_INSTANCES), null, Table.ALIGN_LEFT);
    
    /*for (ManagementProcessDefinition managementDefinition : runningDefinitions.values()) {
      eventTable.addItem(new String[]{managementDefinition.processDefinition.getId(), 
      		managementDefinition.processDefinition.getName(),
      		String.valueOf(managementDefinition.runningInstances.size())}, 
      		managementDefinition.processDefinition.getId());
    }*/
    
    eventLayout.addComponent(eventTable);
  }
}

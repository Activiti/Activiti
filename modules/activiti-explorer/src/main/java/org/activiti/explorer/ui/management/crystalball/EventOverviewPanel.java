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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.crystalball.simulator.ReplaySimulationRun;
import org.activiti.crystalball.simulator.SimpleEventCalendar;
import org.activiti.crystalball.simulator.SimulationDebugger;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventComparator;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogUserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.StartReplayLogEventHandler;
import org.activiti.crystalball.simulator.impl.replay.ReplayUserTaskCompleteEventHandler;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
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
  
  // Process instance start event
  private static final String PROCESS_INSTANCE_START_EVENT_TYPE = "PROCESS_INSTANCE_START";
  private static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";
  private static final String VARIABLES_KEY = "variables";
  // User task completed event
  private static final String USER_TASK_COMPLETED_EVENT_TYPE = "USER_TASK_COMPLETED";
  
  private static final String SIMULATION_BUSINESS_KEY = "testBusinessKey";
  
  protected transient HistoryService historyService;
  protected transient RepositoryService repositoryService;
  protected transient RuntimeService runtimeService;
  protected transient IdentityService identityService;
  protected transient ManagementService managementService;
  protected I18nManager i18nManager;
  protected VariableRendererManager variableRendererManager;

  protected HorizontalLayout instanceLayout;
  protected NativeSelect definitionSelect;
  protected Button replayButton;
  protected Table instanceTable;
  protected HorizontalLayout eventLayout;
  protected Button stepButton;
  protected Button showProcessInstanceButton;
  protected Table eventTable;
  protected Label noMembersTable;
  
  protected List<ProcessDefinition> definitionList;
  protected Map<String, ProcessDefinition> definitionMap = new HashMap<String, ProcessDefinition>();
  protected List<HistoricProcessInstance> instanceList;
  protected List<SimulationEvent> simulationEvents;
  protected HistoricProcessInstance replayHistoricInstance;
  protected SimulationDebugger simulationDebugger;
  
  public EventOverviewPanel() {
  	this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.variableRendererManager = ExplorerApp.get().getVariableRendererManager();
    this.definitionList = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().list();
    this.instanceList = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initializeDefinitionMap();
    init();
    initializeCurrentValues();
  }
  
  protected void initializeDefinitionMap() {
    for (ProcessDefinition definition : definitionList) {
      definitionMap.put(definition.getId(), definition);
    }
  }
  
  protected void initializeCurrentValues() {
    if (ExplorerApp.get().getCrystalBallSimulationDebugger() != null) {
      this.simulationDebugger = ExplorerApp.get().getCrystalBallSimulationDebugger();
      this.simulationEvents = ExplorerApp.get().getCrystalBallSimulationEvents();
      
      String selectedDefinitionId = ExplorerApp.get().getCrystalBallCurrentDefinitionId();
      if (selectedDefinitionId != null) {
        definitionSelect.setValue(selectedDefinitionId);
      }
      
      String selectedInstanceId = ExplorerApp.get().getCrystalBallCurrentInstanceId();
      if (selectedInstanceId != null) {
        instanceTable.setValue(selectedInstanceId);
      }
      
      List<HistoricProcessInstance> replayProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
          .processInstanceBusinessKey(SIMULATION_BUSINESS_KEY)
          .orderByProcessInstanceStartTime()
          .desc()
          .list();
      if (replayProcessInstanceList != null && !replayProcessInstanceList.isEmpty()) {
        replayHistoricInstance = replayProcessInstanceList.get(0);
      }
      
      refreshEvents();
    }
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initProcessInstances();
    initEvents();
  }
  
  protected void initProcessInstances() {
    HorizontalLayout instancesHeader = new HorizontalLayout();
    instancesHeader.setSpacing(false);
    instancesHeader.setMargin(false);
    instancesHeader.setWidth(100, UNITS_PERCENTAGE);
    instancesHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(instancesHeader);
    
    initProcessInstanceTitle(instancesHeader);
    
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
          String selectedDefinitionId = (String) definitionSelect.getValue();
          ExplorerApp.get().setCrystalBallCurrentDefinitionId(selectedDefinitionId);
          refreshInstances(selectedDefinitionId);
        }
      }
    });
    
    selectLayout.addComponent(definitionSelect);
    
    replayButton = new Button(i18nManager.getMessage(Messages.CRYSTALBALL_BUTTON_REPLAY));
    replayButton.setEnabled(false);
    replayButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (instanceTable.getValue() != null) {
          String processInstanceId = (String) instanceTable.getValue();
          ExplorerApp.get().setCrystalBallCurrentInstanceId(processInstanceId);
          List<EventLogEntry> eventLogEntries = managementService.getEventLogEntriesByProcessInstanceId(processInstanceId);
          if (eventLogEntries == null || eventLogEntries.isEmpty()) return;
          EventLogTransformer transformer = new EventLogTransformer(getTransformers());
          simulationEvents = transformer.transform(eventLogEntries);
          ExplorerApp.get().setCrystalBallSimulationEvents(simulationEvents);
          
          SimpleEventCalendar eventCalendar = new SimpleEventCalendar(
              ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration().getClock(), 
              new SimulationEventComparator());
          eventCalendar.addEvents(simulationEvents);
  
          // replay process instance run
          simulationDebugger = new ReplaySimulationRun(ProcessEngines.getDefaultProcessEngine(), 
              eventCalendar, getReplayHandlers(processInstanceId));
          ExplorerApp.get().setCrystalBallSimulationDebugger(simulationDebugger);
          
          simulationDebugger.init(new NoExecutionVariableScope());
          
          simulationDebugger.step();

          // replay process was started
          List<HistoricProcessInstance> replayProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
              .processInstanceBusinessKey(SIMULATION_BUSINESS_KEY)
              .orderByProcessInstanceStartTime()
              .desc()
              .list();
          if (replayProcessInstanceList != null && !replayProcessInstanceList.isEmpty()) {
            replayHistoricInstance = replayProcessInstanceList.get(0);
          }
          
          refreshEvents();
        }
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
    Label titleHeader = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCES));
    titleHeader.addStyleName(ExplorerLayout.STYLE_H3);
    instancesHeader.addComponent(titleHeader);
  }
  
  protected void initInstancesTable() {
    if (instanceList == null || instanceList.isEmpty()) {
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
      instanceTable.addContainerProperty("ended", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ENDED), null, Table.ALIGN_LEFT);
      
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
    eventsHeader.setWidth(80, UNITS_PERCENTAGE);
    eventsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(eventsHeader);
    
    initEventTitle(eventsHeader);
    
    stepButton = new Button(i18nManager.getMessage(Messages.CRYSTALBALL_BUTTON_NEXTEVENT));
    stepButton.setEnabled(false);
    stepButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (!SimulationRunContext.getEventCalendar().getEvents().isEmpty()) {
          simulationDebugger.step();
          refreshEvents();
        }
      }
    });
    eventsHeader.addComponent(stepButton);
    eventsHeader.setComponentAlignment(stepButton, Alignment.MIDDLE_LEFT);
    
    showProcessInstanceButton = new Button();
    showProcessInstanceButton.addStyleName(Reindeer.BUTTON_LINK);
    showProcessInstanceButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        if (replayHistoricInstance != null) {
          ExplorerApp.get().getViewManager().showMyProcessInstancesPage(replayHistoricInstance.getId());
        }
      }
    });
    
    eventsHeader.addComponent(showProcessInstanceButton);
    eventsHeader.setComponentAlignment(showProcessInstanceButton, Alignment.MIDDLE_LEFT);
    
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
    eventTable.setVisible(false);
    eventTable.setWidth(100, UNITS_PERCENTAGE);
    eventTable.setHeight(250, UNITS_PIXELS);
    
    eventTable.setEditable(false);
    eventTable.setImmediate(true);
    eventTable.setSelectable(true);
    eventTable.setSortDisabled(false);
    
    eventTable.addContainerProperty("type", String.class, null, i18nManager.getMessage(Messages.CRYSTALBALL_EVENT_TYPE), null, Table.ALIGN_LEFT);
    eventTable.addContainerProperty("executed", String.class, null, i18nManager.getMessage(Messages.CRYSTALBALL_EVENT_EXECUTED), null, Table.ALIGN_LEFT);
    
    eventLayout.addComponent(eventTable);
  }
  
  protected void refreshEvents() {
    stepButton.setEnabled(false);
    showProcessInstanceButton.setVisible(false);
    eventTable.removeAllItems();
    fillEventValues();
  }
  
  protected void fillEventValues() {
    for (SimulationEvent originalEvent : simulationEvents) {
      boolean executed = true;
      if (SimulationRunContext.getEventCalendar() != null && SimulationRunContext.getEventCalendar().getEvents() != null) {
        for (SimulationEvent event : SimulationRunContext.getEventCalendar().getEvents()) {
          if (originalEvent.equals(event)) {
            executed = false;
            stepButton.setEnabled(true);
            break;
          }
        }
      }
      
      Object itemId = eventTable.addItem();
      eventTable.getItem(itemId).getItemProperty("type").setValue(originalEvent.getType());
      eventTable.getItem(itemId).getItemProperty("executed").setValue(executed);
    }
    
    if (replayHistoricInstance != null && replayHistoricInstance.getId() != null) {
      ProcessInstance testInstance = runtimeService.createProcessInstanceQuery().processInstanceId(replayHistoricInstance.getId()).singleResult();
      if (testInstance != null) {
        showProcessInstanceButton.setCaption(i18nManager.getMessage(
            Messages.TASK_PART_OF_PROCESS, definitionMap.get(replayHistoricInstance.getProcessDefinitionId())));
        showProcessInstanceButton.setVisible(true);
      }
    }
    
    eventTable.setVisible(true);
  }
  
  protected List<Function<EventLogEntry, SimulationEvent>> getTransformers() {
    List<Function<EventLogEntry, SimulationEvent>> transformers = new ArrayList<Function<EventLogEntry, SimulationEvent>>();
    transformers.add(new EventLogProcessInstanceCreateTransformer(PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_DEFINITION_ID_KEY, SIMULATION_BUSINESS_KEY, VARIABLES_KEY));
    transformers.add(new EventLogUserTaskCompleteTransformer(USER_TASK_COMPLETED_EVENT_TYPE));
    return transformers;
  }

  protected Map<String, SimulationEventHandler> getReplayHandlers(String processInstanceId) {
    Map<String, SimulationEventHandler> handlers = new HashMap<String, SimulationEventHandler>();
    handlers.put(PROCESS_INSTANCE_START_EVENT_TYPE, new StartReplayLogEventHandler(processInstanceId, PROCESS_DEFINITION_ID_KEY, SIMULATION_BUSINESS_KEY, VARIABLES_KEY));
    handlers.put(USER_TASK_COMPLETED_EVENT_TYPE, new ReplayUserTaskCompleteEventHandler());
    return handlers;
  }
}

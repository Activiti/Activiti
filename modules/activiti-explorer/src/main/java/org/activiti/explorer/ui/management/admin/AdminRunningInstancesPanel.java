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
package org.activiti.explorer.ui.management.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.custom.UserProfileLink;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.ProcessDefinitionImageStreamResourceBuilder;
import org.activiti.explorer.ui.variable.VariableRendererManager;
import org.activiti.image.ProcessDiagramGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Tijs Rademakers
 */
public class AdminRunningInstancesPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected transient HistoryService historyService;
  protected transient RepositoryService repositoryService;
  protected transient RuntimeService runtimeService;
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;
  protected VariableRendererManager variableRendererManager;

  protected HorizontalLayout definitionsLayout;
  protected Table definitionsTable;
  protected Label noMembersTable;
  protected HorizontalLayout instancesLayout;
  protected Table instancesTable;
  
  protected Map<String, ManagementProcessDefinition> runningDefinitions;
  protected List<HistoricProcessInstance> instanceList;
  protected ManagementProcessDefinition selectedManagementDefinition;
  
  public AdminRunningInstancesPanel() {
  	this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.variableRendererManager = ExplorerApp.get().getVariableRendererManager();
    this.instanceList = historyService.createHistoricProcessInstanceQuery().unfinished().list();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.PANEL_LIGHT);
    
    initPageTitle();
    initDefinitions();
    initInstances();
  }
  
  protected void initPageTitle() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth(100, UNITS_PERCENTAGE);
    layout.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    layout.setSpacing(true);
    layout.setMargin(false, false, true, false);
    addDetailComponent(layout);
    
    Embedded groupImage = new Embedded(null, Images.PROCESS_50);
    layout.addComponent(groupImage);
    
    Label groupName = new Label(i18nManager.getMessage(Messages.ADMIN_RUNNING_TITLE));
    groupName.setSizeUndefined();
    groupName.addStyleName(Reindeer.LABEL_H2);
    layout.addComponent(groupName);
    layout.setComponentAlignment(groupName, Alignment.MIDDLE_LEFT);
    layout.setExpandRatio(groupName, 1.0f);
  }
  
  protected void initDefinitions() {
    HorizontalLayout definitionsHeader = new HorizontalLayout();
    definitionsHeader.setSpacing(true);
    definitionsHeader.setWidth(100, UNITS_PERCENTAGE);
    definitionsHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(definitionsHeader);
    
    initDefinitionTitle(definitionsHeader);
    
    definitionsLayout = new HorizontalLayout();
    definitionsLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(definitionsLayout);
    initDefinitionsTable();
  }
  
  protected void initDefinitionTitle(HorizontalLayout membersHeader) {
    Label usersHeader = new Label(i18nManager.getMessage(Messages.ADMIN_DEFINITIONS));
    usersHeader.addStyleName(ExplorerLayout.STYLE_H3);
    membersHeader.addComponent(usersHeader);
  }
  
  protected void initDefinitionsTable() {
    if(instanceList == null || instanceList.isEmpty()) {
    	noMembersTable = new Label(i18nManager.getMessage(Messages.ADMIN_RUNNING_NONE_FOUND));
      definitionsLayout.addComponent(noMembersTable);
    
    } else {
  	
    	runningDefinitions = new HashMap<String, ManagementProcessDefinition>();
    	for (HistoricProcessInstance instance : instanceList) {
	      String processDefinitionId = instance.getProcessDefinitionId();
	      ManagementProcessDefinition managementDefinition = null;
	      if(runningDefinitions.containsKey(processDefinitionId)) {
	      	managementDefinition = runningDefinitions.get(processDefinitionId);
	      
	      } else {
	      	ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
	      	if (definition == null) {
	      	  // this process has a missing definition - skip
	      	  continue;
	      	}
	      	managementDefinition = new ManagementProcessDefinition();
	      	managementDefinition.processDefinition = definition;
	      	managementDefinition.runningInstances = new ArrayList<HistoricProcessInstance>();
	      	runningDefinitions.put(definition.getId(), managementDefinition);
	      }
	      
	      managementDefinition.runningInstances.add(instance);
      }
    	
      definitionsTable = new Table();
      definitionsTable.setWidth(100, UNITS_PERCENTAGE);
      definitionsTable.setHeight(250, UNITS_PIXELS);
      
      definitionsTable.setEditable(false);
      definitionsTable.setImmediate(true);
      definitionsTable.setSelectable(true);
      definitionsTable.setSortDisabled(false);
      
      definitionsTable.addContainerProperty("id", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ID), null, Table.ALIGN_LEFT);
      definitionsTable.addContainerProperty("name", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_NAME), null, Table.ALIGN_LEFT);
      definitionsTable.addContainerProperty("nr of instances", String.class, null, i18nManager.getMessage(Messages.ADMIN_NR_INSTANCES), null, Table.ALIGN_LEFT);
      
      for (ManagementProcessDefinition managementDefinition : runningDefinitions.values()) {
	      definitionsTable.addItem(new String[]{managementDefinition.processDefinition.getId(), 
	      		managementDefinition.processDefinition.getName(),
	      		String.valueOf(managementDefinition.runningInstances.size())}, 
	      		managementDefinition.processDefinition.getId());
      }
      
      definitionsTable.addListener(new Property.ValueChangeListener() {
        private static final long serialVersionUID = 1L;
        public void valueChange(ValueChangeEvent event) {
          Item item = definitionsTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
          if(item != null) {
            String definitionId = (String) item.getItemProperty("id").getValue();
            selectedManagementDefinition = runningDefinitions.get(definitionId);
            refreshInstancesTable();
          }
        }
      });
      
      definitionsLayout.addComponent(definitionsTable);
    } 
  }
  
  protected void initInstances() {
  	HorizontalLayout instancesHeader = new HorizontalLayout();
  	instancesHeader.setSpacing(true);
  	instancesHeader.setWidth(100, UNITS_PERCENTAGE);
  	instancesHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(instancesHeader);
    initInstancesTitle(instancesHeader);
    
    instancesLayout = new HorizontalLayout();
    instancesLayout.setWidth(100, UNITS_PERCENTAGE);
    addDetailComponent(instancesLayout);
    initInstancesTable();
  }
  
  protected void initInstancesTitle(HorizontalLayout instancesHeader) {
    Label instancesLabel = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCES));
    instancesLabel.addStyleName(ExplorerLayout.STYLE_H3);
    instancesHeader.addComponent(instancesLabel);
  }
  
  protected void initInstancesTable() {
    	
    instancesTable = new Table();
    instancesTable.setWidth(100, UNITS_PERCENTAGE);
    instancesTable.setHeight(250, UNITS_PIXELS);
    
    instancesTable.setEditable(false);
    instancesTable.setImmediate(true);
    instancesTable.setSelectable(true);
    instancesTable.setSortDisabled(false);
    
    instancesTable.addContainerProperty("id", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_ID), null, Table.ALIGN_LEFT);
    instancesTable.addContainerProperty("business key", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_BUSINESSKEY), null, Table.ALIGN_LEFT);
    instancesTable.addContainerProperty("start user id", String.class, null, i18nManager.getMessage(Messages.ADMIN_STARTED_BY), null, Table.ALIGN_LEFT);
    instancesTable.addContainerProperty("start activity id", String.class, null, i18nManager.getMessage(Messages.ADMIN_START_ACTIVITY), null, Table.ALIGN_LEFT);
    instancesTable.addContainerProperty("start time", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_STARTED), null, Table.ALIGN_LEFT);
    
    instancesTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 1L;
      public void valueChange(ValueChangeEvent event) {
        Item item = instancesTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String instanceId = (String) item.getItemProperty("id").getValue();
          
          HistoricProcessInstance processInstance = null;
          for (HistoricProcessInstance instance : selectedManagementDefinition.runningInstances) {
	          if(instance.getId().equals(instanceId)) {
	          	processInstance = instance;
	          	break;
	          }
          }
          
          if(processInstance != null)
          addProcessImage(selectedManagementDefinition.processDefinition, processInstance);
          addTasks(processInstance);
          addVariables(processInstance);
        }
      }
    });
    
    instancesLayout.addComponent(instancesTable);
  }
  
  protected void refreshInstancesTable() {
  	instancesTable.removeAllItems();
  	for (HistoricProcessInstance instance : selectedManagementDefinition.runningInstances) {
  		instancesTable.addItem(new String[]{instance.getId(), 
  				instance.getBusinessKey(),
      		instance.getStartUserId(),
      		instance.getStartActivityId(),
      		instance.getStartTime().toString()}, 
      		instance.getId());
  	}
  }
  
  static class ManagementProcessDefinition implements Serializable {
  	public ProcessDefinition processDefinition;
  	public List<HistoricProcessInstance> runningInstances;
  }
  
  private Embedded currentEmbedded;
  private Label imageHeader;
  
  protected void addProcessImage(ProcessDefinition processDefinition, HistoricProcessInstance processInstance) {
  	if(currentEmbedded != null) {
  		mainPanel.removeComponent(currentEmbedded);
  	}
  	
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
      .getDeployedProcessDefinition(processDefinition.getId());

    // Only show when graphical notation is defined
    if (processDefinitionEntity != null && processDefinitionEntity.isGraphicalNotationDefined()) {
    	
    	if(imageHeader == null) {
	    	imageHeader = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_DIAGRAM));
	    	imageHeader.addStyleName(ExplorerLayout.STYLE_H3);
	    	imageHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
	    	imageHeader.addStyleName(ExplorerLayout.STYLE_NO_LINE);
	    	addDetailComponent(imageHeader);
    	}

    	ProcessEngineConfiguration processEngineConfig = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration();
      ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();

      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        	.buildStreamResource(processInstance.getId(), processInstance.getProcessDefinitionId(), 
        			repositoryService, runtimeService, diagramGenerator, processEngineConfig);

      currentEmbedded = new Embedded(null, diagram);
      currentEmbedded.setType(Embedded.TYPE_IMAGE);
      addDetailComponent(currentEmbedded);
    }
  }

  protected String getProcessDisplayName(ProcessDefinition processDefinition, ProcessInstance processInstance) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName() + " (" + processInstance.getId() +")";
    } else {
      return processDefinition.getKey() + " (" + processInstance.getId() +")";
    }
  }

  private Label tasksHeader;
  private Table taskTable;
  private Label noTasksLabel;
  private Label tasksEmptyHeader;
  
  protected void addTasks(HistoricProcessInstance processInstance) {
    if(tasksHeader != null) {
      mainPanel.removeComponent(tasksHeader);
      mainPanel.removeComponent(tasksEmptyHeader);
    }
    
    if (noTasksLabel != null) {
      mainPanel.removeComponent(noTasksLabel);
    }
 
    tasksHeader = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_HEADER_TASKS));
    tasksHeader.addStyleName(ExplorerLayout.STYLE_H3);
    tasksHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    tasksHeader.addStyleName(ExplorerLayout.STYLE_NO_LINE);
    addDetailComponent(tasksHeader);
    
    tasksEmptyHeader = new Label("&nbsp;", Label.CONTENT_XHTML);
    addDetailComponent(tasksEmptyHeader);
    
  	if(taskTable != null) {
  		mainPanel.removeComponent(taskTable);
  	}
  	
    taskTable = new Table();
    taskTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
    taskTable.setWidth(100, UNITS_PERCENTAGE);
    taskTable.setHeight(250, UNITS_PIXELS);
    
    // Fetch all tasks
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .processInstanceId(processInstance.getId())
      .orderByHistoricTaskInstanceEndTime().desc()
      .orderByTaskCreateTime().desc()
      .list();
    
    if(!tasks.isEmpty()) {
      
      // Finished icon
      taskTable.addContainerProperty("finished", Component.class, null, i18nManager.getMessage(Messages.ADMIN_FINISHED), null, Table.ALIGN_CENTER);
      taskTable.setColumnWidth("finished", 22);
      
      taskTable.addContainerProperty("name", String.class, null, i18nManager.getMessage(Messages.TASK_NAME),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("priority", Integer.class, null, i18nManager.getMessage(Messages.TASK_PRIORITY),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("assignee", Component.class, null, i18nManager.getMessage(Messages.TASK_ASSIGNEE),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("dueDate", Component.class, null, i18nManager.getMessage(Messages.TASK_DUEDATE),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("startDate", Component.class, null, i18nManager.getMessage(Messages.TASK_CREATE_TIME),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("endDate", Component.class, null, i18nManager.getMessage(Messages.TASK_COMPLETE_TIME),
              null, Table.ALIGN_LEFT);
      
      addDetailComponent(taskTable);
      
      for(HistoricTaskInstance task : tasks) {
        addTaskItem(task, taskTable);
      }
      
      taskTable.setPageLength(taskTable.size());
    } else {
      // No tasks
      noTasksLabel = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_NO_TASKS));
      addDetailComponent(noTasksLabel);
    }
  }
  
  protected void addTaskItem(HistoricTaskInstance task, Table taskTable) {
    Item item = taskTable.addItem(task.getId());
    
    if(task.getEndTime() != null) {
      item.getItemProperty("finished").setValue(new Embedded(null, Images.TASK_FINISHED_22));
    } else {
      item.getItemProperty("finished").setValue(new Embedded(null, Images.TASK_22));
    }
    
    item.getItemProperty("name").setValue(task.getName());
    item.getItemProperty("priority").setValue(task.getPriority());
    
    item.getItemProperty("startDate").setValue(new PrettyTimeLabel(task.getStartTime(), true));
    item.getItemProperty("endDate").setValue(new PrettyTimeLabel(task.getEndTime(), true));
    
    if(task.getDueDate() != null) {
      Label dueDateLabel = new PrettyTimeLabel(task.getEndTime(), i18nManager.getMessage(Messages.TASK_NOT_FINISHED_YET), true); 
      item.getItemProperty("dueDate").setValue(dueDateLabel);
    }
    
    if(task.getAssignee() != null) {
      Component taskAssigneeComponent = getTaskAssigneeComponent(task.getAssignee());
      if(taskAssigneeComponent != null) {
        item.getItemProperty("assignee").setValue(taskAssigneeComponent);
      }
    }
  }
  
  protected Component getTaskAssigneeComponent(String assignee) {
    return new UserProfileLink(identityService, true, assignee);
  }
  
  private Label variablesHeader;
  private Label variablesEmptyHeader;
  private Table variablesTable;
  private Label noVariablesLabel;

  protected void addVariables(HistoricProcessInstance processInstance) {
    if(variablesHeader != null) {
      mainPanel.removeComponent(variablesHeader);
      mainPanel.removeComponent(variablesEmptyHeader);
    }
    
    if(noVariablesLabel != null) {
      mainPanel.removeComponent(noVariablesLabel);
    }
    
    variablesHeader = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_HEADER_VARIABLES));
    variablesHeader.addStyleName(ExplorerLayout.STYLE_H3);
    variablesHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    variablesHeader.addStyleName(ExplorerLayout.STYLE_NO_LINE);
    addDetailComponent(variablesHeader);
    
    variablesEmptyHeader = new Label("&nbsp;", Label.CONTENT_XHTML);
    addDetailComponent(variablesEmptyHeader);
  	
  	if(variablesTable != null) {
  		mainPanel.removeComponent(variablesTable);
  	}
    
    // variable sorting is done in-memory (which is ok, since normally there aren't that many vars)
    Map<String, Object> variables = new TreeMap<String, Object>(runtimeService.getVariables(processInstance.getId())); 
    
    if(!variables.isEmpty()) {
      
      variablesTable = new Table();
      variablesTable.setWidth(60, UNITS_PERCENTAGE);
      variablesTable.setHeight(250, UNITS_PIXELS);
      variablesTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
      
      variablesTable.addContainerProperty("name", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_VARIABLE_NAME), null, Table.ALIGN_LEFT);
      variablesTable.addContainerProperty("value", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_VARIABLE_VALUE), null, Table.ALIGN_LEFT);
      
      for (String variable : variables.keySet()) {
        Item variableItem = variablesTable.addItem(variable);
        variableItem.getItemProperty("name").setValue(variable);
        
        // Get string value to show
        String theValue = null;
        try {
          theValue = variableRendererManager.getStringRepresentation(variables.get(variable));
        } catch(Exception e) {
          theValue = "N/A";
        }
        variableItem.getItemProperty("value").setValue(theValue);
      }
      
      variablesTable.setPageLength(variables.size());
      addDetailComponent(variablesTable);
    } else {
      noVariablesLabel = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_NO_VARIABLES));
      addDetailComponent(noVariablesLabel);
    }
  }
}

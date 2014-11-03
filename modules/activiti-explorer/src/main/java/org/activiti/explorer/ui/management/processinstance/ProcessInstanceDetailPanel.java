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

package org.activiti.explorer.ui.management.processinstance;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.custom.UserProfileLink;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.ProcessDefinitionImageStreamResourceBuilder;
import org.activiti.explorer.ui.variable.VariableRendererManager;
import org.activiti.explorer.util.XmlUtil;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class ProcessInstanceDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceDetailPanel.class);

  protected transient RuntimeService runtimeService;
  protected transient RepositoryService repositoryService;
  protected transient TaskService taskService;
  protected transient HistoryService historyService;
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;
  protected VariableRendererManager variableRendererManager;
  
  protected ProcessInstance processInstance;
  protected AbstractTablePage processInstancePage;
  protected HistoricProcessInstance historicProcessInstance;
  protected ProcessDefinition processDefinition;
  
  protected VerticalLayout panelLayout;

  public ProcessInstanceDetailPanel(String processInstanceId, AbstractTablePage processInstancePage) {
    
    // Member initialization
    this.processInstancePage = processInstancePage;

    this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.variableRendererManager = ExplorerApp.get().getVariableRendererManager();

    this.processInstance = getProcessInstance(processInstanceId);
    this.processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
    this.historicProcessInstance = getHistoricProcessInstance(processInstanceId);
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();

    init();
  }

  protected void init() {
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();
    
    panelLayout = new VerticalLayout();
    panelLayout.setWidth(100, UNITS_PERCENTAGE);
    panelLayout.setMargin(true);
    setDetailContainer(panelLayout);

    addHeader();
    addProcessImage();
    addTasks();
    addVariables();
    addDeleteButton();
  }
  
  protected void addHeader() {
    GridLayout header = new GridLayout(3, 2);
    header.setWidth(100, UNITS_PERCENTAGE);
    header.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    header.setSpacing(true);
    header.setMargin(false, false, true, false);
    
    // Add image
    Embedded image = new Embedded(null, Images.PROCESS_50);
    header.addComponent(image, 0, 0, 0, 1);
    
    // Add task name
    Label nameLabel = new Label(getProcessDisplayName(processDefinition, processInstance));
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    header.addComponent(nameLabel, 1, 0, 2, 0);

    // Add start time
    PrettyTimeLabel startTimeLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.PROCESS_START_TIME),
      historicProcessInstance.getStartTime(), null, true);
    startTimeLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_START_TIME);
    header.addComponent(startTimeLabel, 1, 1);
    
    header.setColumnExpandRatio(1, 1.0f);
    header.setColumnExpandRatio(2, 1.0f);
    
    panelLayout.addComponent(header);
  }
  
  protected void addProcessImage() {
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
      .getDeployedProcessDefinition(processDefinition.getId());

    // Only show when graphical notation is defined
    if (processDefinitionEntity != null) {
      
      boolean didDrawImage = false;
      
      if (ExplorerApp.get().isUseJavascriptDiagram()) {
        try {
          
          final InputStream definitionStream = repositoryService.getResourceAsStream(
              processDefinition.getDeploymentId(), processDefinition.getResourceName());
          XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
          XMLStreamReader xtr = xif.createXMLStreamReader(definitionStream);
          BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
          
          if (!bpmnModel.getFlowLocationMap().isEmpty()) {
            
            int maxX = 0;
            int maxY = 0;
            for (String key : bpmnModel.getLocationMap().keySet()) {
              GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(key);
              double elementX = graphicInfo.getX() + graphicInfo.getWidth();
              if (maxX < elementX) {
                maxX = (int) elementX;
              }
              double elementY = graphicInfo.getY() + graphicInfo.getHeight();
              if (maxY < elementY) {
                maxY = (int) elementY;
              }
            }
            
            Panel imagePanel = new Panel(); // using panel for scrollbars
            imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
            imagePanel.setWidth(100, UNITS_PERCENTAGE);
            imagePanel.setHeight(100, UNITS_PERCENTAGE);
            URL explorerURL = ExplorerApp.get().getURL();
            URL url = new URL(explorerURL.getProtocol(), explorerURL.getHost(), explorerURL.getPort(), explorerURL.getPath().replace("/ui", "") +
                "diagram-viewer/index.html?processDefinitionId=" + processDefinition.getId() + "&processInstanceId=" + processInstance.getId());
            Embedded browserPanel = new Embedded("", new ExternalResource(url));
            browserPanel.setType(Embedded.TYPE_BROWSER);
            browserPanel.setWidth(maxX + 350 + "px");
            browserPanel.setHeight(maxY + 220 + "px");
            
            HorizontalLayout panelLayoutT = new HorizontalLayout();
            panelLayoutT.setSizeUndefined();
            imagePanel.setContent(panelLayoutT);
            imagePanel.addComponent(browserPanel);
            
            panelLayout.addComponent(imagePanel);
            
            didDrawImage = true;
          }
          
        } catch (Exception e) {
          LOGGER.error("Error loading process diagram component", e);
        }
      }
      
      if(!didDrawImage && processDefinitionEntity.isGraphicalNotationDefined()) {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
          .buildStreamResource(processInstance, repositoryService, runtimeService, diagramGenerator, processEngineConfiguration);
  
        if(diagram != null) {
          Label header = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_DIAGRAM));
          header.addStyleName(ExplorerLayout.STYLE_H3);
          header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
          header.addStyleName(ExplorerLayout.STYLE_NO_LINE);
          panelLayout.addComponent(header);
          
          Embedded embedded = new Embedded(null, diagram);
          embedded.setType(Embedded.TYPE_IMAGE);
          embedded.setSizeUndefined();
    
          Panel imagePanel = new Panel(); // using panel for scrollbars
          imagePanel.setScrollable(true);
          imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
          imagePanel.setWidth(100, UNITS_PERCENTAGE);
          imagePanel.setHeight(100, UNITS_PERCENTAGE);
          
          HorizontalLayout panelLayoutT = new HorizontalLayout();
          panelLayoutT.setSizeUndefined();
          imagePanel.setContent(panelLayoutT);
          imagePanel.addComponent(embedded);
          
          panelLayout.addComponent(imagePanel);
        }
      }
    }
  }

  protected String getProcessDisplayName(ProcessDefinition processDefinition, ProcessInstance processInstance) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName() + " (" + processInstance.getId() +")";
    } else {
      return processDefinition.getKey() + " (" + processInstance.getId() +")";
    }
  }

  protected void addTasks() {
    Label header = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_HEADER_TASKS));
    header.addStyleName(ExplorerLayout.STYLE_H3);
    header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    header.addStyleName(ExplorerLayout.STYLE_NO_LINE);
    panelLayout.addComponent(header);
    
    panelLayout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    
    Table taskTable = new Table();
    taskTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
    taskTable.setWidth(100, UNITS_PERCENTAGE);
    
    // Fetch all tasks
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .processInstanceId(processInstance.getId())
      .orderByHistoricTaskInstanceEndTime().desc()
      .orderByHistoricTaskInstanceStartTime().desc()
      .list();
    
    if(!tasks.isEmpty()) {
      
      // Finished icon
      taskTable.addContainerProperty("finished", Component.class, null, "", null, Table.ALIGN_CENTER);
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
      
      panelLayout.addComponent(taskTable);
      panelLayout.setExpandRatio(taskTable, 1.0f);
      
      for(HistoricTaskInstance task : tasks) {
        addTaskItem(task, taskTable);
      }
      
      taskTable.setPageLength(taskTable.size());
    } else {
      // No tasks
      Label noTaskLabel = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_NO_TASKS));
      panelLayout.addComponent(noTaskLabel);
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

  protected void addVariables() {
    Label header = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_HEADER_VARIABLES));
    header.addStyleName(ExplorerLayout.STYLE_H3);
    header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    header.addStyleName(ExplorerLayout.STYLE_NO_LINE);
    panelLayout.addComponent(header);
    
    panelLayout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    
    // variable sorting is done in-memory (which is ok, since normally there aren't that many vars)
    Map<String, Object> variables = new TreeMap<String, Object>(runtimeService.getVariables(processInstance.getId())); 
    
    if(!variables.isEmpty()) {
      
      Table variablesTable = new Table();
      variablesTable.setWidth(60, UNITS_PERCENTAGE);
      variablesTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
      
      variablesTable.addContainerProperty("name", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_VARIABLE_NAME), null, Table.ALIGN_LEFT);
      variablesTable.addContainerProperty("value", String.class, null, i18nManager.getMessage(Messages.PROCESS_INSTANCE_VARIABLE_VALUE), null, Table.ALIGN_LEFT);
      
      for (String variable : variables.keySet()) {
        Item variableItem = variablesTable.addItem(variable);
        variableItem.getItemProperty("name").setValue(variable);
        
        // Get string value to show
        String theValue = variableRendererManager.getStringRepresentation(variables.get(variable));
        variableItem.getItemProperty("value").setValue(theValue);
      }
      
      variablesTable.setPageLength(variables.size());
      panelLayout.addComponent(variablesTable);
    } else {
      Label noVariablesLabel = new Label(i18nManager.getMessage(Messages.PROCESS_INSTANCE_NO_VARIABLES));
      panelLayout.addComponent(noVariablesLabel);
    }
  }
  
  protected void addDeleteButton() {
    Button deleteProcessInstanceButton = new Button(i18nManager.getMessage(Messages.PROCESS_INSTANCE_DELETE));
    deleteProcessInstanceButton.setIcon(Images.DELETE);
    deleteProcessInstanceButton.addListener(new DeleteProcessInstanceClickListener(processInstance.getId(), processInstancePage));
    
    // Clear toolbar and add 'start' button
    processInstancePage.getToolBar().removeAllButtons();
    processInstancePage.getToolBar().addButton(deleteProcessInstanceButton);
  }
  
  protected ProcessInstance getProcessInstance(String processInstanceId) {
    return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
  }

  protected HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
    return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
  }

  protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
  }

}

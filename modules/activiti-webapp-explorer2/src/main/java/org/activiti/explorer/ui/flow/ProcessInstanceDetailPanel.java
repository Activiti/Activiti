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

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
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
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.custom.UserProfileLink;

import com.vaadin.data.Item;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Frederik Heremans
 * 
 */
public class ProcessInstanceDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1705077407829697827L;

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected I18nManager i18nManager;
  
  protected ProcessInstancePage myFlowsPage;
  protected ProcessInstance processInstance;
  protected HistoricProcessInstance historicProcessInstance;
  protected ProcessDefinition processDefinition;
  
  protected VerticalLayout verticalLayout;

  public ProcessInstanceDetailPanel(String processInstanceId, ProcessInstancePage myFlowsPage) {
    this.myFlowsPage = myFlowsPage;

    runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();

    processInstance = getProcessInstance(processInstanceId);
    processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
    historicProcessInstance = getHistoricProcessInstance(processInstanceId);

    // Initialize UI
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();
    
    verticalLayout = new VerticalLayout();
    verticalLayout.setWidth(100, UNITS_PERCENTAGE);
    verticalLayout.setMargin(true);
    setDetailContainer(verticalLayout);

    addName();
    addFlowImage();
    addTasks();
  }

  protected void addTasks() {
    Label header = new Label(i18nManager.getMessage(Messages.FLOW_INSTANCE_HEADER_TASKS));
    header.addStyleName(ExplorerLayout.STYLE_H3);
    header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    verticalLayout.addComponent(header);
    
    Label spacer = new Label();
    spacer.setValue("&nbsp");
    spacer.setContentMode(Label.CONTENT_XHTML);
    verticalLayout.addComponent(spacer);
    
    Table taskTable = new Table();
    taskTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
    taskTable.setWidth(100, UNITS_PERCENTAGE);
    
    // Fetch all tasks
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .processInstanceId(processInstance.getId())
      .orderByHistoricTaskInstanceEndTime().desc()
      .orderByHistoricActivityInstanceStartTime().desc()
      .list();
    
    if(tasks.size() > 0) {
      
      // Finished icon
      taskTable.addContainerProperty("finished", Component.class, null, "", null, Table.ALIGN_CENTER);
      taskTable.setColumnWidth("finished", 16);
      
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
      
      verticalLayout.addComponent(taskTable);
      verticalLayout.setExpandRatio(taskTable, 1.0f);
      
      for(HistoricTaskInstance task : tasks) {
        addTaskItem(task, taskTable);
      }
      
      taskTable.setPageLength(taskTable.size());
    } else {
      // No tasks
      Label noTaskLabel = new Label(i18nManager.getMessage(Messages.FLOW_INSTANCE_NO_TASKS));
      verticalLayout.addComponent(noTaskLabel);
    }
  }
  
  protected void addTaskItem(HistoricTaskInstance task, Table taskTable) {
    Item item = taskTable.addItem(task.getId());
    
    if(task.getEndTime() != null) {
      item.getItemProperty("finished").setValue(new Embedded(null, Images.TASK_22));
    } else {
      item.getItemProperty("finished").setValue(new Embedded(null, Images.TASK_FINISHED_22));
    }
    
    item.getItemProperty("name").setValue(task.getName());
    item.getItemProperty("priority").setValue(task.getPriority());
    
    item.getItemProperty("startDate").setValue(new PrettyTimeLabel(task.getStartTime()));
    item.getItemProperty("endDate").setValue(new PrettyTimeLabel(task.getEndTime()));
    
    if(task.getDueDate() != null) {
      Label dueDateLabel = new PrettyTimeLabel(task.getEndTime(), i18nManager.getMessage(Messages.TASK_NOT_FINISHED_YET)); 
      item.getItemProperty("dueDate").setValue(dueDateLabel);
    }
    
    if(task.getAssignee() != null) {
      Component assignee = new UserProfileLink(identityService, true, task.getAssignee());
      item.getItemProperty("assignee").setValue(assignee);
    }
  }

  protected void addFlowImage() {
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
      .getDeployedProcessDefinition(processDefinition.getId());

    // Only show when graphical notation is defined
    if (processDefinitionEntity != null && processDefinitionEntity.isGraphicalNotationDefined()) {
      Label header = new Label(i18nManager.getMessage(Messages.FLOW_INSTANCE_HEADER_DIAGRAM));
      header.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
      header.addStyleName(ExplorerLayout.STYLE_H3);
      verticalLayout.addComponent(header);
      
      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        .buildStreamResource(processInstance, repositoryService, runtimeService);

      Embedded embedded = new Embedded("", diagram);
      embedded.setType(Embedded.TYPE_IMAGE);
      verticalLayout.addComponent(embedded);
    }
  }

  protected void addName() {
    GridLayout header = new GridLayout(3, 2);
    header.setWidth(100, UNITS_PERCENTAGE);
    header.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    header.setSpacing(true);
    header.setMargin(false, false, true, false);
    
    // Add image
    Embedded image = new Embedded(null, Images.FLOW_50);
    header.addComponent(image, 0, 0, 0, 1);
    
    // Add task name
    Label nameLabel = new Label(processDefinition.getName() + " (" + processInstance.getId() +")");
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    header.addComponent(nameLabel, 1, 0, 2, 0);

    // Add start time
    PrettyTimeLabel startTimeLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.FLOW_START_TIME),
      historicProcessInstance.getStartTime(), null);
    startTimeLabel.addStyleName(ExplorerLayout.STYLE_FLOW_HEADER_START_TIME);
    header.addComponent(startTimeLabel, 1, 1);
    
    header.setColumnExpandRatio(1, 1.0f);
    header.setColumnExpandRatio(2, 1.0f);
    
    verticalLayout.addComponent(header);
  }
  

  private ProcessInstance getProcessInstance(String processInstanceId) {
    return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
  }

  private HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
    return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
  }

  private ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
  }
}

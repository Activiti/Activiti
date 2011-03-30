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

import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.Constant;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.util.PrettyTimeColumnGenerator;
import org.activiti.explorer.ui.util.ThemedImageBooleanColumnGenerator;
import org.activiti.explorer.ui.util.UserProfilePopupLinkColumnGenerator;
import org.activiti.explorer.util.ItemListContainer;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Frederik Heremans
 * 
 */
public class ProcessInstanceDetailPanel extends Panel {

  private static final long serialVersionUID = 1705077407829697827L;

  protected ProcessInstancePage myFlowsPage;
  protected ProcessInstance processInstance;
  protected HistoricProcessInstance historicProcessInstance;
  protected ProcessDefinition processDefinition;

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  public ProcessInstanceDetailPanel(String processInstanceId, ProcessInstancePage myFlowsPage) {
    this.myFlowsPage = myFlowsPage;

    runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();

    processInstance = getProcessInstance(processInstanceId);
    processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
    historicProcessInstance = getHistoricProcessInstance(processInstanceId);

    // Initialize UI
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();

    addName();
    addTimeDetails();
    addFlowImage();
    addTasks();
  }

  protected void addTasks() {
    Label header = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_INSTANCE_HEADER_TASKS));
    header.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_DETAILS_HEADER);
    addComponent(header);
    
    Table taskTable = new Table();
    taskTable.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_TASK_LIST);
    taskTable.setWidth(100, UNITS_PERCENTAGE);
    taskTable.setHeight(100, UNITS_PERCENTAGE);
    
    // Fetch all tasks
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .processInstanceId(processInstance.getId())
      .orderByHistoricTaskInstanceEndTime().desc()
      .orderByHistoricActivityInstanceStartTime().desc()
      .list();
    
    if(tasks.size() > 0) {
      ItemListContainer simpleContainer = new ItemListContainer();
      for(HistoricTaskInstance task : tasks) {
        simpleContainer.addItem(createTaskItem(task));
      }
      taskTable.setContainerDataSource(simpleContainer);
      
      // Finished icon
      taskTable.addGeneratedColumn("finished", new ThemedImageBooleanColumnGenerator(Images.TASK_FINISHED, Images.TASK_UNFINISHED));
      taskTable.setColumnHeader("finished", "");
      taskTable.setColumnWidth("finished", 16);
      
      // Task name
      taskTable.addContainerProperty("name", String.class, null, ExplorerApplication.getCurrent().getMessage(Messages.TASK_NAME),
              null, Table.ALIGN_LEFT);
      taskTable.addContainerProperty("priority", Integer.class, null, ExplorerApplication.getCurrent().getMessage(Messages.TASK_PRIORITY),
              null, Table.ALIGN_LEFT);
      
      // Assignee link
      taskTable.addGeneratedColumn("assignee", new UserProfilePopupLinkColumnGenerator(identityService, true));
      taskTable.setColumnHeader("assignee", ExplorerApplication.getCurrent().getMessage(Messages.TASK_ASSIGNEE));
      
      // Timing
      taskTable.addGeneratedColumn("dueDate", new PrettyTimeColumnGenerator());
      taskTable.setColumnHeader("dueDate", ExplorerApplication.getCurrent().getMessage(Messages.TASK_DUEDATE));
      taskTable.addGeneratedColumn("startDate", new PrettyTimeColumnGenerator());
      taskTable.setColumnHeader("startDate", ExplorerApplication.getCurrent().getMessage(Messages.TASK_CREATE_TIME));
      taskTable.addGeneratedColumn("endDate", new PrettyTimeColumnGenerator(ExplorerApplication.getCurrent().getMessage(Messages.TASK_NOT_FINISHED_YET)));
      taskTable.setColumnHeader("endDate", ExplorerApplication.getCurrent().getMessage(Messages.TASK_COMPLETE_TIME));
      
      addComponent(taskTable);
      ((VerticalLayout) getContent()).setExpandRatio(taskTable, 1.0f);
      ((VerticalLayout) getContent()).setSizeFull();
    } else {
      // No tasks
      Label noTaskLabel = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_INSTANCE_NO_TASKS));
      addComponent(noTaskLabel);
    }
  }
  
  protected Item createTaskItem(HistoricTaskInstance task) {
    PropertysetItem item = new PropertysetItem();
    item.addItemProperty("id", new ObjectProperty<String>(task.getId()));
    item.addItemProperty("name", new ObjectProperty<String>(task.getName()));
    item.addItemProperty("startDate", new ObjectProperty<Date>(task.getStartTime()));
    item.addItemProperty("priority", new ObjectProperty<Integer>(task.getPriority()));
    
    boolean finished = task.getEndTime() != null;
    item.addItemProperty("finished", new ObjectProperty<Boolean>(finished));

    if(finished) {
      item.addItemProperty("endDate", new ObjectProperty<Date>(task.getEndTime()));
    }
    if(task.getDueDate() != null) {
      item.addItemProperty("dueDate", new ObjectProperty<Date>(task.getDueDate()));
    }
    if(task.getAssignee() != null) {
      item.addItemProperty("assignee", new ObjectProperty<String>(task.getAssignee()));
    }
    
    return item;
  }

  protected void addFlowImage() {
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
      .getDeployedProcessDefinition(processDefinition.getId());

    // Only show when graphical notation is defined
    if (processDefinitionEntity != null && processDefinitionEntity.isGraphicalNotationDefined()) {
      Label header = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_INSTANCE_HEADER_DIAGRAM));
      header.addStyleName(ExplorerLayout.STYLE_PROCESS_INSTANCE_DETAILS_HEADER);
      addComponent(header);
      
      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        .buildStreamResource(processInstance, repositoryService, runtimeService);

      Embedded embedded = new Embedded("", diagram);
      embedded.setType(Embedded.TYPE_IMAGE);
      addComponent(embedded);
    }
  }

  protected void addTimeDetails() {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);

    HorizontalLayout timeDetails = new HorizontalLayout();
    timeDetails.setSpacing(true);
    addComponent(timeDetails);

    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetails.addComponent(clockImage);

    String startedOnDate = new PrettyTime().format(historicProcessInstance.getStartTime());
    String startedOn = ExplorerApplication.getCurrent().getMessage(Messages.FLOW_INSTANCE_STARTED_ON, startedOnDate);
    Label timeLabel = new Label(startedOn);
    timeDetails.addComponent(timeLabel);

    SimpleDateFormat format = (SimpleDateFormat) Constant.DEFAULT_DATE_FORMATTER.clone();
    String dateString = format.format(historicProcessInstance.getStartTime());

    Label realCreateTime = new Label("(" + dateString + ")");
    realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
    realCreateTime.setSizeUndefined();
    timeDetails.addComponent(realCreateTime);
    timeDetails.setComponentAlignment(realCreateTime, Alignment.MIDDLE_CENTER);
    timeDetails.setComponentAlignment(timeLabel, Alignment.MIDDLE_CENTER);
  }

  protected void addName() {
    Label nameLabel = new Label(processDefinition.getName() + " (" + processInstance.getId() + ")");
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    addComponent(nameLabel);
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

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
package org.activiti.explorer.ui.task;

import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.form.FormPropertiesEventListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.task.listener.ClaimTaskClickListener;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * The central panel on the task page, showing all the details of a task.
 * 
 * @author Joram Barrez
 */
public class TaskDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;

  protected Task task;
  
  // Services
  protected TaskService taskService;
  protected FormService formService;
  protected RepositoryService repositoryService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  
  // UI
  protected TaskPage taskPage;
  protected VerticalLayout centralLayout;
  protected FormPropertiesForm taskForm;
  protected TaskInvolvedPeopleComponent involvedPeople;
  protected SubTaskComponent subTaskComponent;
  protected TaskRelatedContentComponent relatedContent;
  protected Button completeButton;
  protected Button claimButton;
  
  public TaskDetailPanel(Task task, TaskPage taskPage) {
    this.task = task;
    this.taskPage = taskPage;
    
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();

    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    // Central panel: all task data
    this.centralLayout = new VerticalLayout();
    centralLayout.setMargin(true);
    setDetailContent(centralLayout);
    
    initHeader();
    initDescriptionAndClaimButton();
    initProcessLink();
    initParentTaskLink();
    initPeopleDetails();
    initSubTasks();
    initRelatedContent();
    initTaskForm();
    
  }
  
  protected void initHeader() {
    GridLayout taskDetails = new GridLayout(5, 2);
    taskDetails.setWidth(100, UNITS_PERCENTAGE);
    taskDetails.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    taskDetails.setSpacing(true);
    
    // Add image
    Embedded image = new Embedded(null, Images.TASK_50);
    taskDetails.addComponent(image, 0, 0, 0, 1);
    
    // Add task name
    Label nameLabel = new Label(task.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    taskDetails.addComponent(nameLabel, 1, 0, 4,0);

    // Add due date
    PrettyTimeLabel dueDateLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.TASK_DUEDATE_SHORT),
      task.getDueDate(), i18nManager.getMessage(Messages.TASK_DUEDATE_UNKNOWN));
    dueDateLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_DUEDATE);
    taskDetails.addComponent(dueDateLabel, 1, 1);
    
    // Add priority
    Integer lowMedHighPriority = convertPriority(task.getPriority());
    Label priorityLabel = new Label();
    switch(lowMedHighPriority) {
    case 1:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_LOW));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_LOW);
      break;
    case 2:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_MEDIUM));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_MEDIUM);
      break;
    case 3:
    default:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_HIGH));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_HIGH);
    }
    taskDetails.addComponent(priorityLabel, 2, 1);
    
    // Add create date
    PrettyTimeLabel createLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.TASK_CREATED_SHORT),
      task.getCreateTime(), "");
    createLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_CREATE_TIME);
    taskDetails.addComponent(createLabel, 3, 1);
    
    // Add label to fill excess space
    Label spacer = new Label();
    spacer.setContentMode(Label.CONTENT_XHTML);
    spacer.setValue("&nbsp;");
    spacer.setSizeUndefined();
    taskDetails.addComponent(spacer);
    
    taskDetails.setColumnExpandRatio(1, 1.0f);
    taskDetails.setColumnExpandRatio(2, 1.0f);
    taskDetails.setColumnExpandRatio(3, 1.0f);
    taskDetails.setColumnExpandRatio(4, 1.0f);
    centralLayout.addComponent(taskDetails);
  }
  
  protected void initDescriptionAndClaimButton() {
    addEmptySpace(centralLayout);
    HorizontalLayout descriptionLayout = new HorizontalLayout();
    descriptionLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    descriptionLayout.setWidth(100, UNITS_PERCENTAGE);
    descriptionLayout.setSpacing(true);
    
    if(!isCurrentUserAssignee() && canUserClaimTask()) {
      claimButton = new Button(i18nManager.getMessage(Messages.TASK_CLAIM));
      claimButton.addListener(new ClaimTaskClickListener(task.getId(), taskService));
      descriptionLayout.addComponent(claimButton);
      descriptionLayout.setComponentAlignment(claimButton, Alignment.MIDDLE_LEFT);
    }
    
    if (task.getDescription() != null) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLayout.addComponent(descriptionLabel);
      descriptionLayout.setExpandRatio(descriptionLabel, 1.0f);
      descriptionLayout.setComponentAlignment(descriptionLabel, Alignment.MIDDLE_LEFT);
    }
    
    centralLayout.addComponent(descriptionLayout);
  }

  protected void initProcessLink() {
    if(task.getProcessInstanceId() != null) {
      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(task.getProcessDefinitionId())
        .singleResult();
      
      Button showProcessInstanceButton = new Button(i18nManager.getMessage(
        Messages.TASK_PART_OF_PROCESS, processDefinition.getName()));
      showProcessInstanceButton.addStyleName(Reindeer.BUTTON_LINK);
      showProcessInstanceButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showMyFlowsPage(task.getProcessInstanceId());
        }
      });
     
      centralLayout.addComponent(showProcessInstanceButton);
      addEmptySpace(centralLayout);
    }
  }
  
  protected void initParentTaskLink() {
    if (task.getParentTaskId() != null) {
      final Task parentTask = taskService.createTaskQuery()
        .taskId(task.getParentTaskId()).singleResult();
      
      Button showParentTaskButton = new Button(i18nManager.getMessage(
              Messages.TASK_SUBTASK_OF_PARENT_TASK, parentTask.getName()));
      showParentTaskButton.addStyleName(Reindeer.BUTTON_LINK);
      showParentTaskButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showInboxPage(parentTask.getId());
        }
      });
      
      centralLayout.addComponent(showParentTaskButton);
      addEmptySpace(centralLayout);
    }
  }
  
  protected void initPeopleDetails() {
    involvedPeople = new TaskInvolvedPeopleComponent(task, this);
    centralLayout.addComponent(involvedPeople);
  }
  
  
  protected void initSubTasks() {
    subTaskComponent = new SubTaskComponent(task);
    centralLayout.addComponent(subTaskComponent);
  }
  
  protected void initRelatedContent() {
    relatedContent = new TaskRelatedContentComponent(task, this);
    centralLayout.addComponent(relatedContent);
  }
  
  protected void initTaskForm() {
    // Check if task requires a form
    TaskFormData formData = formService.getTaskFormData(task.getId());
    if(formData != null && formData.getFormProperties() != null && formData.getFormProperties().size() > 0) {
      taskForm = new FormPropertiesForm();
      taskForm.setSubmitButtonCaption(i18nManager.getMessage(Messages.TASK_COMPLETE));
      taskForm.setCancelButtonCaption(i18nManager.getMessage(Messages.TASK_RESET_FORM));
      taskForm.setFormProperties(formData.getFormProperties());
      
      taskForm.addListener(new FormPropertiesEventListener() {
        
        private static final long serialVersionUID = -3893467157397686736L;

        @Override
        protected void handleFormSubmit(FormPropertiesEvent event) {
          Map<String, String> properties = event.getFormProperties();
          formService.submitTaskFormData(task.getId(), properties);
          notificationManager.showInformationNotification(Messages.TASK_COMPLETED, task.getName());
          taskPage.refreshListSelectNext();
        }
        
        @Override
        protected void handleFormCancel(FormPropertiesEvent event) {
          // Clear the form values 
          taskForm.clear();
        }
      });
      // Only if current user is task's assignee
      taskForm.setEnabled(isCurrentUserAssignee());
      
      // Add component to page
      centralLayout.addComponent(taskForm);
    } else {
      // Just add a button to complete the task
      // TODO: perhaps move to a better place
      
      CssLayout buttonLayout = new CssLayout();
      buttonLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
      buttonLayout.setWidth(100, UNITS_PERCENTAGE);
      centralLayout.addComponent(buttonLayout);
      
      completeButton = new Button(i18nManager.getMessage(Messages.TASK_COMPLETE));
      
      completeButton.addListener(new ClickListener() {
        
        private static final long serialVersionUID = 1L;

        public void buttonClick(ClickEvent event) {
          taskService.complete(task.getId());     
          notificationManager.showInformationNotification(Messages.TASK_COMPLETED, task.getName());
          taskPage.refreshListSelectNext();
        }
      });
      
      completeButton.setEnabled(isCurrentUserAssignee());
      buttonLayout.addComponent(completeButton);
    }
  }

  protected boolean isCurrentUserAssignee() {
    String currentUser = ExplorerApp.get().getLoggedInUser().getId();
    return currentUser.equals(task.getAssignee());
  }
  
  protected boolean canUserClaimTask() {
   return taskService.createTaskQuery()
     .taskCandidateUser(ExplorerApp.get().getLoggedInUser().getId())
     .taskId(task.getId())
     .count() == 1; 
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
  /**
   * Returns a numeric priority - low (1), medium (2) or high (3) - 
   * based on the given numeric priority value.
   */
  protected Integer convertPriority(int priority) {
    // TODO: define thresholds
    return 1;
  }
  
  public void notifyPeopleInvolvedChanged() {
    involvedPeople.refreshPeopleGrid();
    taskPage.getTaskEventPanel().refreshTaskEvents();
  }
  
  public void notifyAssigneeChanged() {
    involvedPeople.refreshAssignee();
    taskPage.getTaskEventPanel().refreshTaskEvents();
  }
  
  public void notifyOwnerChanged() {
    involvedPeople.refreshOwner();
    taskPage.getTaskEventPanel().refreshTaskEvents();
  }
  
  public void notifyRelatedContentChanged() {
    relatedContent.refreshTaskAttachments();
    taskPage.getTaskEventPanel().refreshTaskEvents();
  }
  
}

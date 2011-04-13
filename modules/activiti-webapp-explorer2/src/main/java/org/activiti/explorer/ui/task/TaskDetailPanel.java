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
import org.activiti.explorer.ui.form.FormPropertiesEventListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.task.listener.ClaimTaskClickListener;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;


/**
 * The central panel on the task page, showing all the details of a task.
 * 
 * @author Joram Barrez
 */
public class TaskDetailPanel extends HorizontalLayout {
  
  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected Task task;
  
  // Services
  protected TaskService taskService;
  protected FormService formService;
  protected RepositoryService repositoryService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  
  // UI
  protected TaskPage parent;
  protected Panel centralPanel;
  protected TaskEventsPanel eventPanel;
  protected FormPropertiesForm taskForm;
  protected TaskInvolvedPeopleComponent involvedPeople;
  protected TaskRelatedContentComponent relatedContent;
  protected Button completeButton;
  protected Button claimButton;
  
  public TaskDetailPanel(String taskId, TaskPage parent) {
    this.parent = parent;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    
    this.taskId = taskId;
    this.task = taskService.createTaskQuery().taskId(taskId).singleResult();
    
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();

    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    // Central panel: all task data
    this.centralPanel = new Panel();
    centralPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(centralPanel);
    setExpandRatio(centralPanel, 75.0f);
    
    // Right panel: the task comments
    this.eventPanel = new TaskEventsPanel(task);
    eventPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(eventPanel);
    setExpandRatio(eventPanel, 25.0f);
    
    initName();
    initDescription();
    initProcessLink();
    initClaimButton();
    initTimeDetails();
    initPeopleDetails();
    initRelatedContent();
    initTaskForm();
  }
  
  protected void initName() {
    Label nameLabel = new Label(task.getName() + " - " + task.getId());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    centralPanel.addComponent(nameLabel);
  }
  
  protected void initDescription() {
    addEmptySpace(centralPanel);
    
    if (task.getDescription() != null) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
      centralPanel.addComponent(descriptionLabel);
    }
    
    addEmptySpace(centralPanel);
  }
  
  protected void initClaimButton() {
    if(!isCurrentUserAssignee() && canUserClaimTask()) {
      claimButton = new Button(i18nManager.getMessage(Messages.TASK_CLAIM));
      claimButton.addListener(new ClaimTaskClickListener(task.getId(), taskService));
      
      centralPanel.addComponent(claimButton);
      addEmptySpace(centralPanel);
    }
  }

  protected void initProcessLink() {
    if(task.getProcessInstanceId() != null) {
      ProcessDefinition processDefinition = getProcessDefinition(task.getProcessDefinitionId());
      
      ClickListener clickListener = new ClickListener() {
        private static final long serialVersionUID = 7250731154745638326L;

        public void buttonClick(ClickEvent event) {
          viewManager.showMyFlowsPage(task.getProcessInstanceId());
        }
      };
      
      Button showProcessInstanceButton = new Button(i18nManager.getMessage(
        Messages.TASK_PART_OF_PROCESS, processDefinition.getName(), task.getProcessInstanceId()), clickListener);
      showProcessInstanceButton.setSizeUndefined();
      showProcessInstanceButton.addStyleName(Reindeer.BUTTON_LINK);
     
      centralPanel.addComponent(showProcessInstanceButton);
      addEmptySpace(centralPanel);
    }
  }
  
  protected void initTimeDetails() {
    HorizontalLayout timeDetailsLayout = new HorizontalLayout();
    timeDetailsLayout.setSpacing(true);
    timeDetailsLayout.setSizeUndefined();
    centralPanel.addComponent(timeDetailsLayout);

    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetailsLayout.addComponent(clockImage);

    // The other time fields are layed out in a 2 column grid
    GridLayout grid = new GridLayout();
    grid.addStyleName(ExplorerLayout.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setColumns(2);

    timeDetailsLayout.addComponent(grid);
    timeDetailsLayout.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);

    // create time
    if (task.getCreateTime() != null) {
      Label createTime = new Label(i18nManager.getMessage(Messages.TASK_CREATED) + " " + new PrettyTime().format(task.getCreateTime()));
      createTime.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      createTime.setSizeUndefined();
      grid.addComponent(createTime);
      
      Label realCreateTime = new Label("(" + task.getCreateTime() + ")");
      realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
      realCreateTime.setSizeUndefined();
      grid.addComponent(realCreateTime);
    }

    // due date
    if (task.getDueDate() != null) {
      Label dueDate = new Label(i18nManager.getMessage(Messages.TASK_DUEDATE) + " " + new PrettyTime().format(task.getDueDate())); 
      dueDate.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      dueDate.setSizeUndefined();
      grid.addComponent(dueDate);

      Label realDueDateTime = new Label("(" + task.getDueDate() + ")");
      realDueDateTime.addStyleName(Reindeer.LABEL_SMALL);
      realDueDateTime.setSizeUndefined();
      grid.addComponent(realDueDateTime);
    }
  }

  protected void initPeopleDetails() {
    involvedPeople = new TaskInvolvedPeopleComponent(task, this);
    centralPanel.addComponent(involvedPeople);
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
          parent.refreshList();
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
      centralPanel.addComponent(taskForm);
    } else {
      // Just add a button to complete the task
      // TODO: perhaps move to a better place
      completeButton = new Button(i18nManager.getMessage(Messages.TASK_COMPLETE));
      
      completeButton.addListener(new ClickListener() {
        
        private static final long serialVersionUID = 1L;

        public void buttonClick(ClickEvent event) {
          taskService.complete(task.getId());     
          notificationManager.showInformationNotification(Messages.TASK_COMPLETED, task.getName());
          parent.refreshList();
        }
      });
      
      addEmptySpace(centralPanel);
      
      completeButton.setEnabled(isCurrentUserAssignee());
      centralPanel.addComponent(completeButton);
    }
  }
  
  protected void initRelatedContent() {
    addEmptySpace(centralPanel);
    relatedContent = new TaskRelatedContentComponent(task, this);
    centralPanel.addComponent(relatedContent);
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
  
  protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .singleResult();
  }
  
  public void notifyPeopleInvolvedChanged() {
    involvedPeople.refreshPeopleGrid();
    eventPanel.refreshTaskEvents();
  }
  
  public void notifyAssigneeChanged() {
    involvedPeople.refreshAssignee();
    eventPanel.refreshTaskEvents();
  }
  
  public void notifyOwnerChanged() {
    involvedPeople.refreshOwner();
    eventPanel.refreshTaskEvents();
  }
  
  public void notifyRelatedContentChanged() {
    relatedContent.refreshTaskAttachments();
    eventPanel.refreshTaskEvents();
  }
  
}

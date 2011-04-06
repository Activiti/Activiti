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
 * @author Joram Barrez
 */
public class TaskDetailPanel extends HorizontalLayout {
  
  private static final long serialVersionUID = -2018798598805436750L;

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
  protected Panel leftPanel;
  protected Panel rightPanel;
  protected FormPropertiesForm taskForm;
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
    
    
    // left panel: all details about the task
    this.leftPanel = new Panel();
    leftPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(leftPanel);
    setExpandRatio(leftPanel, 75.0f);
    
    initName();
    initDescription();
    initProcessLink();
    initClaimButton();
    initTimeDetails();
    initPeopleDetails();
    initRelatedContent();
    initTaskForm();
    
    
    // Right panel: the task comments
    this.rightPanel = new TaskCommentPanel(taskId);
    rightPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(rightPanel);
    setExpandRatio(rightPanel, 25.0f);
  }
  
  protected void initName() {
    Label nameLabel = new Label(task.getName() + " - " + task.getId());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    leftPanel.addComponent(nameLabel);
  }
  
  protected void initDescription() {
    addEmptySpace(leftPanel);
    
    if (task.getDescription() != null) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
      leftPanel.addComponent(descriptionLabel);
    }
    
    addEmptySpace(leftPanel);
  }
  
  protected void initClaimButton() {
    if(!isCurrentUserAssignee() && canUserClaimTask()) {
      claimButton = new Button(i18nManager.getMessage(Messages.TASK_CLAIM));
      claimButton.addListener(new ClaimTaskClickListener(task.getId(), taskService));
      
      leftPanel.addComponent(claimButton);
      addEmptySpace(leftPanel);
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
     
      leftPanel.addComponent(showProcessInstanceButton);
      addEmptySpace(leftPanel);
    }
  }
  
  protected void initTimeDetails() {
    HorizontalLayout timeDetailsLayout = new HorizontalLayout();
    timeDetailsLayout.setSpacing(true);
    timeDetailsLayout.setSizeUndefined();
    leftPanel.addComponent(timeDetailsLayout);

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
    // first add some empty space for aesthetics
    addEmptySpace(leftPanel);
    
    // Layout for involved people
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setSizeUndefined();
    leftPanel.addComponent(layout);
    
    // people icon
    Embedded peopleImage = new Embedded(null, Images.PEOPLE);
    layout.addComponent(peopleImage);
    
    // The involved people are layed out in a grid with two rows
    GridLayout grid = new GridLayout();
    grid.addStyleName(ExplorerLayout.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setRows(2);
    
    layout.addComponent(grid);
    layout.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);
    
    // owner
    if (task.getOwner() != null) {
      Button owner = new Button(task.getOwner() + " (owner)");
      owner.addStyleName(Reindeer.BUTTON_LINK);
      owner.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showProfilePopup(task.getOwner());
        }
      });
      
      grid.addComponent(owner);
    }
    
    // assignee
    if (task.getAssignee() != null) {
      Button assignee = new Button(task.getAssignee() + " (assignee)");
      assignee.addStyleName(Reindeer.BUTTON_LINK);
      assignee.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showProfilePopup(task.getAssignee());
        }
      });
      grid.addComponent(assignee);
    }
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
      leftPanel.addComponent(taskForm);
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
      
      addEmptySpace(leftPanel);
      
      completeButton.setEnabled(isCurrentUserAssignee());
      leftPanel.addComponent(completeButton);
    }
  }
  
  protected void initRelatedContent() {
    addEmptySpace(leftPanel);
    
    relatedContent = new TaskRelatedContentComponent(task);
    
    leftPanel.addComponent(relatedContent);
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
}

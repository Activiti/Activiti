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

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.task.listener.DeleteSubTaskClickListener;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Custom component for the 'sub tasks' section for a task.
 * Used in the {@link TaskDetailPanel}.
 * 
 * @author Joram Barrez
 */
public class SubTaskComponent extends CustomComponent {
  
  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected TaskService taskService;
  protected HistoryService historyService;
  
  protected Task parentTask;
  protected TaskDetailPanel taskDetailPanel;
  protected VerticalLayout layout;
  protected Label title;
  protected Panel addSubTaskPanel;
  protected Button addSubTaskButton;
  protected TextField newTaskTextField;
  protected GridLayout subTaskLayout;

  public SubTaskComponent(Task parentTask) {
    this.parentTask = parentTask;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    
    initUi();
  }
  
  protected void initUi() {
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addStyleName(ExplorerLayout.STYLE_INVOLVE_PEOPLE);
    
    initLayout();
    initHeader();
    initSubTasks();
  }
  
  protected void initLayout() {
    this.layout = new VerticalLayout();
    setCompositionRoot(layout);
  }
  
  protected void initHeader() {
    HorizontalLayout headerLayout = new HorizontalLayout();
    headerLayout.setWidth(100, UNITS_PERCENTAGE);
    layout.addComponent(headerLayout);
    
    initTitle(headerLayout);
    initAddSubTaskPanel(headerLayout);
  }
  
  protected void initTitle(HorizontalLayout headerLayout) {
    title = new Label(i18nManager.getMessage(Messages.TASK_SUBTASKS));
    title.addStyleName(ExplorerLayout.STYLE_H3);
    title.setWidth(100, UNITS_PERCENTAGE);
    headerLayout.addComponent(title);
    headerLayout.setExpandRatio(title, 1.0f);
  }

  protected void initAddSubTaskPanel(HorizontalLayout headerLayout) {
    // The add button is placed in a panel, so we can catch 'enter' and 'escape' events
    addSubTaskPanel = new Panel();
    addSubTaskPanel.setContent(new VerticalLayout());
    addSubTaskPanel.setSizeUndefined();
    addSubTaskPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addSubTaskPanel.addStyleName("no-border");
    headerLayout.addComponent(addSubTaskPanel);
    
    initAddSubTaskPanelKeyboardActions();
    initAddButton();
  }

  protected void initAddSubTaskPanelKeyboardActions() {
    addSubTaskPanel.addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        if ("escape".equals(action.getCaption())) {
          resetAddButton();
        } else if ("enter".equals(action.getCaption())) {
          if (newTaskTextField != null && newTaskTextField.getValue() != null
                  && !"".equals(newTaskTextField.getValue().toString())) {
            
            LoggedInUser loggedInUser = ExplorerApp.get().getLoggedInUser();
            
            // save task
            Task newTask = taskService.newTask();
            newTask.setParentTaskId(parentTask.getId());
            if (parentTask.getAssignee() != null) {
              newTask.setAssignee(parentTask.getAssignee());
            } else {
              newTask.setAssignee(loggedInUser.getId());
            }
            if (parentTask.getOwner() != null) {
              newTask.setOwner(parentTask.getOwner());
            } else {
              newTask.setOwner(loggedInUser.getId());
            }
            newTask.setName(newTaskTextField.getValue().toString());
            taskService.saveTask(newTask);
            
            // Reset the add button to its original state
            resetAddButton();
            
            // refresh sub tasks section
            refreshSubTasks();
          }
        }
      }
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {
                new ShortcutAction("enter", ShortcutAction.KeyCode.ENTER, null),
                new ShortcutAction("escape", ShortcutAction.KeyCode.ESCAPE, null)
        };
      }
    });
  }
  
  protected void initAddButton() {
    addSubTaskButton = new Button();
    addSubTaskButton.addStyleName(ExplorerLayout.STYLE_ADD);
    addSubTaskPanel.addComponent(addSubTaskButton);
    addSubTaskButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        // Remove button
        addSubTaskPanel.removeAllComponents();
        
        // And add textfield
        Label createSubTaskLabel = new Label("Create new subtask:");
        createSubTaskLabel.addStyleName(Reindeer.LABEL_SMALL);
        addSubTaskPanel.addComponent(createSubTaskLabel);
        newTaskTextField = new TextField();
        newTaskTextField.focus();
        addSubTaskPanel.addComponent(newTaskTextField);
      }
    });
  }
  
  protected void resetAddButton() {
    addSubTaskPanel.removeAllComponents();
    initAddButton();
  }
  
  protected void initSubTasks() {
    List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery()
      .taskParentTaskId(parentTask.getId())
      .list();
    initSubTasksLayout();
    populateSubTasks(subTasks);
  }
  
  protected void initSubTasksLayout() {
    subTaskLayout = new GridLayout();
    subTaskLayout.setColumns(3);
    subTaskLayout.addStyleName(ExplorerLayout.STYLE_TASK_SUBTASKS_LIST);
    subTaskLayout.setWidth(99, UNITS_PERCENTAGE);
    subTaskLayout.setColumnExpandRatio(2, 1.0f);
    subTaskLayout.setSpacing(true);
    layout.addComponent(subTaskLayout);
  }
  
  protected void populateSubTasks(List<HistoricTaskInstance> subTasks) {
    if (subTasks.size() > 0) {
      for (final HistoricTaskInstance subTask : subTasks) {
        // icon
        Embedded icon = null;
        
        if(subTask.getEndTime() != null) {
          icon = new Embedded(null, Images.TASK_FINISHED_22);
        } else {
          icon = new Embedded(null, Images.TASK_22);
        }
        icon.setWidth(22, UNITS_PIXELS);
        icon.setWidth(22, UNITS_PIXELS);
        subTaskLayout.addComponent(icon);
        
        // Link to subtask
        Button subTaskLink = new Button(subTask.getName());
        subTaskLink.addStyleName(Reindeer.BUTTON_LINK);
        subTaskLink.addListener(new ClickListener() {
          public void buttonClick(ClickEvent event) {
            ExplorerApp.get().getViewManager().showTaskPage(subTask.getId());
          }
        });
        subTaskLayout.addComponent(subTaskLink);
        subTaskLayout.setComponentAlignment(subTaskLink, Alignment.MIDDLE_LEFT);
        
        if(subTask.getEndTime() == null) {
          // Delete icon only appears when task is not finished yet
          Embedded deleteIcon = new Embedded(null, Images.DELETE);
          deleteIcon.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
          deleteIcon.addListener(new DeleteSubTaskClickListener(subTask, this));
          subTaskLayout.addComponent(deleteIcon);
          subTaskLayout.setComponentAlignment(deleteIcon, Alignment.MIDDLE_RIGHT);
        } else {
          // Next line of grid
          subTaskLayout.newLine();
        }
      }
    } else {
      Label noSubTasksLabel = new Label(i18nManager.getMessage(Messages.TASK_NO_SUBTASKS));
      noSubTasksLabel.setSizeUndefined();
      noSubTasksLabel.addStyleName(Reindeer.LABEL_SMALL);
      subTaskLayout.addComponent(noSubTasksLabel);
    }
    
  }
  
  public void refreshSubTasks() {
    subTaskLayout.removeAllComponents();
    List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery()
      .taskParentTaskId(parentTask.getId())
      .list();
    populateSubTasks(subTasks);
  }

}

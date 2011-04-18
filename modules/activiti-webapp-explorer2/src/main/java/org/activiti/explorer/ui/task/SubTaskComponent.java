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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
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
  
  protected Task parentTask;
  protected VerticalLayout layout;
  protected Label title;
  protected Panel addSubTaskPanel;
  protected Button addSubTaskButton;
  protected TextField newTaskTextField;

  public SubTaskComponent(Task parentTask) {
    this.parentTask = parentTask;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    initUi();
  }
  
  protected void initUi() {
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addStyleName(ExplorerLayout.STYLE_INVOLVE_PEOPLE);
    
    initLayout();
    initHeader();
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
          addSubTaskPanel.removeAllComponents();
          initAddButton();
        } else if ("enter".equals(action.getCaption())) {
          if (newTaskTextField != null && newTaskTextField.getValue() != null
                  && !"".equals(newTaskTextField.getValue().toString())) {
            Task newTask = taskService.newTask();
            newTask.setAssignee(parentTask.getAssignee());
            newTask.setOwner(parentTask.getOwner());
            newTask.setName(newTaskTextField.getValue().toString());
            taskService.saveTask(newTask);
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

}

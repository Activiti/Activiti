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

import java.util.Arrays;
import java.util.Date;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;


/**
 * Popup window to create a new task
 * 
 * @author Joram Barrez
 */
public class NewCasePopupWindow extends PopupWindow {

  private static final long serialVersionUID = 1L;
  
  protected TaskService taskService;
  protected I18nManager i18nManager;
  
//  protected HorizontalLayout layout;
  protected Form form;
  protected TextField nameField;
  protected TextArea descriptionArea;
  protected DateField dueDateField;
  protected PriorityComboBox priorityComboBox;
  protected Button createTaskButton;
  
  public NewCasePopupWindow() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setModal(true);
    center();
    setResizable(false);
    setCaption(i18nManager.getMessage(Messages.TASK_NEW));
    addStyleName(Reindeer.WINDOW_LIGHT);
    setWidth(430, UNITS_PIXELS);
    setHeight(320, UNITS_PIXELS);
    
    initForm();
    initCreateTaskButton();
    initEnterKeyListener();
  }
  
  protected void initForm() {
    form = new Form();
    form.setValidationVisibleOnCommit(true);
    form.setImmediate(true);
    addComponent(form);
    
    // name
    nameField = new TextField(i18nManager.getMessage(Messages.TASK_NAME));
    nameField.focus();
    nameField.setRequired(true);
    nameField.setRequiredError(i18nManager.getMessage(Messages.TASK_NAME_REQUIRED));
    form.addField("name", nameField);
    
    // description
    descriptionArea = new TextArea(i18nManager.getMessage(Messages.TASK_DESCRIPTION));
    descriptionArea.setColumns(25);
    form.addField("description", descriptionArea);
    
    // duedate
    dueDateField = new DateField(i18nManager.getMessage(Messages.TASK_DUEDATE));
    dueDateField.setResolution(DateField.RESOLUTION_DAY);
    form.addField("duedate", dueDateField);
    
    // priority
    priorityComboBox = new PriorityComboBox(i18nManager);
    form.addField("priority", priorityComboBox);
  }
  
  protected void initCreateTaskButton() {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setWidth(100, UNITS_PERCENTAGE);
    form.getFooter().setWidth(100, UNITS_PERCENTAGE);
    form.getFooter().addComponent(buttonLayout);
    
    Button createButton = new Button(i18nManager.getMessage(Messages.BUTTON_CREATE));
    buttonLayout.addComponent(createButton);
    buttonLayout.setComponentAlignment(createButton, Alignment.BOTTOM_RIGHT);
    
    createButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
      handleFormSubmit();
      }
    });
  }
  
  protected void initEnterKeyListener() {
    addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        handleFormSubmit();
      }
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {new ShortcutAction("enter", ShortcutAction.KeyCode.ENTER, null)};
      }
    });
  }
  
  protected void handleFormSubmit() {
    try {
      // Check for errors
      form.commit(); // will throw exception in case validation is false
      
      // Create task
      Task task = taskService.newTask();
      task.setName(nameField.getValue().toString());
      task.setDescription(descriptionArea.getValue().toString());
      task.setDueDate((Date) dueDateField.getValue());
      task.setPriority(priorityComboBox.getPriority());
      task.setOwner(ExplorerApp.get().getLoggedInUser().getId());
      taskService.saveTask(task);
      
      // close popup and navigate to new group
      close();
      ExplorerApp.get().getViewManager().showTasksPage(task.getId());
      
    } catch (InvalidValueException e) {
      // Do nothing: the Form component will render the errormsgs automatically
      setHeight(350, UNITS_PIXELS);
    }
  }
  
}

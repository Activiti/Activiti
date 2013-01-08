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
package org.activiti.explorer.ui.process.simple.editor.table;

import java.util.ArrayList;
import java.util.List;

import org.activiti.explorer.ui.process.simple.editor.listener.AddTaskClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.DeleteTaskClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.ShowFormClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.TaskFormModelListener;
import org.activiti.workflow.simple.definition.FormDefinition;
import org.activiti.workflow.simple.definition.HumanStepDefinition;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

/**
 * @author Joram Barrez
 */
public class TaskTable extends Table implements TaskFormModelListener {

  private static final long serialVersionUID = -2578437667358797351L;

  protected TaskFormModel taskFormModel = new TaskFormModel();

  public TaskTable() {
    this.taskFormModel.addFormModelListener(this);

    setEditable(true);
    setColumnReorderingAllowed(true);

    setSizeFull();
    setPageLength(0);

    addContainerProperty("name", String.class, null);
    addContainerProperty("assignee", String.class, null);
    addContainerProperty("groups", String.class, null);
    addContainerProperty("description", TextField.class, null);
    addContainerProperty("startWithPrevious", CheckBox.class, null);
    addContainerProperty("actions", HorizontalLayout.class, null);

    setColumnHeader("name", "Name");
    setColumnHeader("assignee", "Assignee");
    setColumnHeader("groups", "Group(s)");
    setColumnHeader("description", "Description");
    setColumnHeader("startWithPrevious", "Concurrency");
    setColumnHeader("actions", "Actions");

    setColumnAlignment("name", ALIGN_CENTER);
    setColumnAlignment("assignee", ALIGN_CENTER);
    setColumnAlignment("groups", ALIGN_CENTER);
    setColumnAlignment("description", ALIGN_CENTER);
    setColumnAlignment("startsWithPrevious", ALIGN_CENTER);

    setColumnWidth("actions", 170);
  }

  // TODO: reactivate when having the option to edit
//  public void addTaskRow(KickstartUserTask task) {
//    Object taskItemId = addTaskRow(null, task.getName(), task.getAssignee(), task.getGroups(), task.getDescription(), task.getStartsWithPrevious());
//    if (task.getForm() != null) {
//      taskFormModel.addForm(taskItemId, task.getForm());
//    }
//  }

  public void addDefaultTaskRow() {
    addDefaultTaskRowAfter(null);
  }

  public void addDefaultTaskRowAfter(Object itemId) {
    addTaskRow(itemId, null, null, null, null, null);
  }

  protected Object addTaskRow(Object previousTaskItemId, String taskName, String taskAssignee, 
          String taskGroups, String taskDescription, Boolean startWithPrevious) {
    
    Object newItemId = null;
    if (previousTaskItemId == null) { // add at the end of list
      newItemId = addItem();
    } else {
      newItemId = addItemAfter(previousTaskItemId);
    }
    Item newItem = getItem(newItemId);

    // name
    newItem.getItemProperty("name").setValue(taskName == null ? "my task" : taskName);

    // assignee
    newItem.getItemProperty("assignee").setValue(taskAssignee == null ? "" : taskAssignee);
    
    // groups
    newItem.getItemProperty("groups").setValue(taskGroups == null ? "" : taskGroups);

    // description
    TextField descriptionTextField = new TextField();
    descriptionTextField.setColumns(16);
    descriptionTextField.setRows(1);
    if (taskDescription != null) {
      descriptionTextField.setValue(taskDescription);
    }
    newItem.getItemProperty("description").setValue(descriptionTextField);

    // concurrency
    CheckBox startWithPreviousCheckBox = new CheckBox("start with previous");
    startWithPreviousCheckBox.setValue(startWithPrevious == null ? false : startWithPrevious);
    newItem.getItemProperty("startWithPrevious").setValue(startWithPreviousCheckBox);

    // actions
    newItem.getItemProperty("actions").setValue(generateActionButtons(newItemId));

    return newItemId;
  }

  protected HorizontalLayout generateActionButtons(Object taskItemId) {
    HorizontalLayout actionButtons = new HorizontalLayout();

    FormDefinition form = taskFormModel.getForm(taskItemId);
    Button formButton = new Button(form == null ? "Create form" : "Edit form");
    formButton.addListener(new ShowFormClickListener(taskFormModel, taskItemId));
    formButton.setData(taskItemId);
    actionButtons.addComponent(formButton);

    Button deleteTaskButton = new Button("-");
    deleteTaskButton.setData(taskItemId);
    deleteTaskButton.addListener(new DeleteTaskClickListener(this));
    actionButtons.addComponent(deleteTaskButton);

    Button addTaskButton = new Button("+");
    addTaskButton.setData(taskItemId);
    addTaskButton.addListener(new AddTaskClickListener(this));
    actionButtons.addComponent(addTaskButton);

    return actionButtons;
  }

  public List<HumanStepDefinition> getSteps() {
    List<HumanStepDefinition> steps = new ArrayList<HumanStepDefinition>();
    for (Object itemId : getItemIds()) {
      Item item = getItem(itemId);

      HumanStepDefinition humanStepDefinition = new HumanStepDefinition();
      humanStepDefinition.setName((String) item.getItemProperty("name").getValue());
      humanStepDefinition.setAssignee((String) item.getItemProperty("assignee").getValue());
//      humanStepDefinition.setGroups((String) item.getItemProperty("groups").getValue());
      humanStepDefinition.setDescription((String) ((TextField) item.getItemProperty("description").getValue()).getValue());
      humanStepDefinition.setStartsWithPrevious((boolean) ((CheckBox) item.getItemProperty("startWithPrevious").getValue()).booleanValue());
      humanStepDefinition.setForm(taskFormModel.getForm(itemId));

      steps.add(humanStepDefinition);
    }
    return steps;
  }
  
  /** Implements FormModelListener */
  public void formAdded(Object taskItemId) {
    getItem(taskItemId).getItemProperty("actions").setValue(generateActionButtons(taskItemId));
  }

  /** Implements FormModelListener */
  public void formRemoved(Object taskItemId) {
    getItem(taskItemId).getItemProperty("actions").setValue(generateActionButtons(taskItemId));
  }

}

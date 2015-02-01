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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.process.simple.editor.listener.AddTaskClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.DeleteTaskClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.ShowFormClickListener;
import org.activiti.explorer.ui.process.simple.editor.listener.TaskFormModelListener;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

/**
 * @author Joram Barrez
 */
public class TaskTable extends Table implements TaskFormModelListener {

  private static final long serialVersionUID = -2578437667358797351L;
  
  public static final String ID_NAME = "name";
  public static final String ID_ASSIGNEE = "assignee";
  public static final String ID_GROUPS = "groups";
  public static final String ID_DESCRIPTION = "description";
  public static final String ID_START_WITH_PREVIOUS = "startWithPrevious";
  public static final String ID_ACTIONS = "actions";

  protected I18nManager i18nManager;
  
  protected TaskFormModel taskFormModel = new TaskFormModel();

  public TaskTable() {
    this.i18nManager  = ExplorerApp.get().getI18nManager();
    this.taskFormModel.addFormModelListener(this);

    setEditable(true);
    setColumnReorderingAllowed(true);

    setSizeFull();
    setPageLength(0);

    addContainerProperty(ID_NAME, String.class, null);
    addContainerProperty(ID_ASSIGNEE, ComboBox.class, null);
    addContainerProperty(ID_GROUPS, ComboBox.class, null);
    addContainerProperty(ID_DESCRIPTION, TextField.class, null);
    addContainerProperty(ID_START_WITH_PREVIOUS, CheckBox.class, null);
    addContainerProperty(ID_ACTIONS, HorizontalLayout.class, null);

    setColumnHeader(ID_NAME, i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_NAME));
    setColumnHeader(ID_ASSIGNEE, i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_ASSIGNEE));
    setColumnHeader(ID_GROUPS, i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_GROUPS));
    setColumnHeader(ID_DESCRIPTION, i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_DESCRIPTION));
    setColumnHeader(ID_START_WITH_PREVIOUS, i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_CONCURRENCY));
    setColumnHeader(ID_ACTIONS, i18nManager.getMessage(Messages.PROCESS_EDITOR_ACTIONS));

    setColumnAlignment(ID_NAME, ALIGN_CENTER);
    setColumnAlignment(ID_ASSIGNEE, ALIGN_CENTER);
    setColumnAlignment(ID_GROUPS, ALIGN_CENTER);
    setColumnAlignment(ID_START_WITH_PREVIOUS, ALIGN_CENTER);
    setColumnAlignment(ID_START_WITH_PREVIOUS, ALIGN_CENTER);
    setColumnWidth(ID_ACTIONS, 170);
  }

  public void addTaskRow(HumanStepDefinition humanStepDefinition) {
    Object taskItemId = addTaskRow(null, humanStepDefinition.getName(), humanStepDefinition.getAssignee(), 
            getCommaSeperated(humanStepDefinition.getCandidateGroups()), humanStepDefinition.getDescription(),
            humanStepDefinition.isStartsWithPrevious());
    if (humanStepDefinition.getForm() != null) {
      taskFormModel.addForm(taskItemId, humanStepDefinition.getForm());
    }
  }

  protected String getCommaSeperated(List<String> list) {
	  if(list != null && !list.isEmpty()) {
	  	return StringUtils.join(list, ", ");
	  }
	  return null;
  }

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
    newItem.getItemProperty(ID_NAME).setValue(taskName == null ? "my task" : taskName);

    // assignee
    ComboBox assigneeComboBox = new ComboBox();
    assigneeComboBox.setNullSelectionAllowed(true);
   
    try {
      for (User user : ProcessEngines.getDefaultProcessEngine().getIdentityService().createUserQuery().orderByUserFirstName().asc().list()) {
	    assigneeComboBox.addItem(user.getId());
	    assigneeComboBox.setItemCaption(user.getId(), user.getFirstName() + " " + user.getLastName());
	  }
    } catch(Exception e) { 
    	// Don't do anything. Will be an empty dropdown.
    }
    
    if (taskAssignee != null) {
      assigneeComboBox.select(taskAssignee);
    }
    
    newItem.getItemProperty(ID_ASSIGNEE).setValue(assigneeComboBox);
    
    // groups
    ComboBox groupComboBox = new ComboBox();
    groupComboBox.setNullSelectionAllowed(true);
    
    try {
      for (Group group : ProcessEngines.getDefaultProcessEngine().getIdentityService().createGroupQuery().orderByGroupName().asc().list()) {
        groupComboBox.addItem(group.getId());
        groupComboBox.setItemCaption(group.getId(), group.getName());
      }
    } catch (Exception e) {
    	// Don't do anything. Will be an empty dropdown.
    }
    
    if (taskGroups != null) {
      groupComboBox.select(taskGroups);
    }
    
    newItem.getItemProperty(ID_GROUPS).setValue(groupComboBox);

    // description
    TextField descriptionTextField = new TextField();
    descriptionTextField.setColumns(16);
    descriptionTextField.setRows(1);
    if (taskDescription != null) {
      descriptionTextField.setValue(taskDescription);
    }
    newItem.getItemProperty(ID_DESCRIPTION).setValue(descriptionTextField);

    // concurrency
    CheckBox startWithPreviousCheckBox = new CheckBox(i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_START_WITH_PREVIOUS));
    startWithPreviousCheckBox.setValue(startWithPrevious == null ? false : startWithPrevious);
    newItem.getItemProperty(ID_START_WITH_PREVIOUS).setValue(startWithPreviousCheckBox);

    // actions
    newItem.getItemProperty(ID_ACTIONS).setValue(generateActionButtons(newItemId));

    return newItemId;
  }

  protected HorizontalLayout generateActionButtons(Object taskItemId) {
    HorizontalLayout actionButtons = new HorizontalLayout();

    FormDefinition form = taskFormModel.getForm(taskItemId);
    Button formButton = new Button(form == null ? i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_FORM_CREATE) :
        i18nManager.getMessage(Messages.PROCESS_EDITOR_TASK_FORM_EDIT));
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
      
      String name = (String) item.getItemProperty(ID_NAME).getValue();
      if (name != null && name.length() > 0) {
        humanStepDefinition.setName(name);
      }
      
      String assignee = (String) ((ComboBox) item.getItemProperty(ID_ASSIGNEE).getValue()).getValue();
      if (assignee != null && assignee.length() > 0) {
        humanStepDefinition.setAssignee(assignee);
      }
      
      String groups = (String) ((ComboBox) item.getItemProperty("groups").getValue()).getValue();
      List<String> candidateGroups = new ArrayList<String>();
      if (groups != null && groups.length() > 0) {
        for (String group : groups.split(",")) {
          candidateGroups.add(group.trim());
        }
      }
      humanStepDefinition.setCandidateGroups(candidateGroups);

      String description = (String) ((TextField) item.getItemProperty(ID_DESCRIPTION).getValue()).getValue();
      if (description != null && description.length() > 0) {
        humanStepDefinition.setDescription(description);
      }
      
      humanStepDefinition.setStartsWithPrevious((boolean) ((CheckBox) item.getItemProperty(ID_START_WITH_PREVIOUS).getValue()).booleanValue());
      
      FormDefinition formDefinition = taskFormModel.getForm(itemId);
      humanStepDefinition.setForm(formDefinition);

      steps.add(humanStepDefinition);
    }
    return steps;
  }
  
  /** Implements FormModelListener */
  public void formAdded(Object taskItemId) {
    getItem(taskItemId).getItemProperty(ID_ACTIONS).setValue(generateActionButtons(taskItemId));
  }

  /** Implements FormModelListener */
  public void formRemoved(Object taskItemId) {
    getItem(taskItemId).getItemProperty(ID_ACTIONS).setValue(generateActionButtons(taskItemId));
  }

}

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
package org.activiti.explorer.ui.process.simple.editor;


import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.process.simple.editor.table.PropertyTable;
import org.activiti.explorer.ui.process.simple.editor.table.TaskFormModel;
import org.activiti.workflow.simple.definition.form.DatePropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.NumberPropertyDefinition;
import org.activiti.workflow.simple.definition.form.TextPropertyDefinition;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Joram Barrez
 */
public class FormPopupWindow extends Window {

  protected static final long serialVersionUID = -1754225937375971709L;

  protected static final String TITLE = "Define form";
  protected static final String DESCRIPTION = "Define the form properties that will be shown with the task";

  protected Object taskItemId;
  protected TaskFormModel formModel;
  protected PropertyTable propertyTable;

  public FormPopupWindow(Object taskItemId, TaskFormModel formModel) {
    this.taskItemId = taskItemId;
    this.formModel = formModel;

    setModal(true);
    setWidth("50%");
    center();
    setCaption(TITLE);

    initUi();
  }

  protected void initUi() {
    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    addComponent(layout);

    // Description
    layout.addComponent(new Label(DESCRIPTION));

    // Property table
    propertyTable = new PropertyTable();
    layout.addComponent(propertyTable);
    fillFormFields();

    // Buttons
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.setSpacing(true);

    // Save button
    Button saveButton = new Button(ExplorerApp.get().getI18nManager().getMessage(Messages.BUTTON_SAVE));
    buttons.addComponent(saveButton);
    saveButton.addListener(new Button.ClickListener() {

      private static final long serialVersionUID = -2906886872414089331L;

      public void buttonClick(ClickEvent event) {
        FormDefinition form = createForm();
        formModel.addForm(taskItemId, form);
        close();
      }
    });

    // Delete button
    Button deleteButton = new Button(ExplorerApp.get().getI18nManager().getMessage(Messages.BUTTON_DELETE));
    buttons.addComponent(deleteButton);
    deleteButton.addListener(new Button.ClickListener() {

      private static final long serialVersionUID = 5267967369680365653L;

      public void buttonClick(ClickEvent event) {
        formModel.removeForm(taskItemId);
        close();
      }
    });

    layout.addComponent(new Label(""));
    layout.addComponent(buttons);
  }

  public FormDefinition createForm() {
    FormDefinition formDefinition = new FormDefinition();
    for (Object itemId : propertyTable.getItemIds()) {
    	
    	Item item = propertyTable.getItem(itemId);
      FormPropertyDefinition formPropertyDefinition = getFormPropertyDefinition(item);
      formDefinition.addFormProperty(formPropertyDefinition);
    }
    return formDefinition;
  }

  protected FormPropertyDefinition getFormPropertyDefinition(Item item) {
  	String type = (String) ((ComboBox) item.getItemProperty(PropertyTable.ID_PROPERTY_TYPE).getValue()).getValue();
  	
  	FormPropertyDefinition result = null;
  	if(type.equals("number")) {
  		result = new NumberPropertyDefinition();
  	} else if(type.equals("date")) {
  		result = new DatePropertyDefinition();
  	} else {
  		result = new TextPropertyDefinition();
  	}
  	
  	// Set generic properties
  	result.setName((String) item.getItemProperty(PropertyTable.ID_PROPERTY_NAME).getValue());
  	result.setMandatory((Boolean) ((CheckBox) item.getItemProperty(PropertyTable.ID_PROPERTY_REQUIRED).getValue()).getValue());
  	
  	return result;
  }

	protected void fillFormFields() {
    FormDefinition form = formModel.getForm(taskItemId);
    if (form == null) {
      propertyTable.addPropertyRow();
    } else {
      for (FormPropertyDefinition property : form.getFormPropertyDefinitions()) {
        propertyTable.addPropertyRow(property.getName(), getPropertyTypeDisplay(property), property.isMandatory());
      }
    }
  }
	
	protected String getPropertyTypeDisplay(FormPropertyDefinition definition) {
	  if(definition instanceof NumberPropertyDefinition) {
			return "number";
		} else if(definition instanceof DatePropertyDefinition) {
			return "date";
		} else {
			// Default
			return "text";
		}
	}

}

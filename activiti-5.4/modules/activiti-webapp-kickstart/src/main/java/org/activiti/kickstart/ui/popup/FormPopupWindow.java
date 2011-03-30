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
package org.activiti.kickstart.ui.popup;

import org.activiti.kickstart.dto.FormDto;
import org.activiti.kickstart.dto.FormPropertyDto;
import org.activiti.kickstart.model.TaskFormModel;
import org.activiti.kickstart.ui.table.PropertyTable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

/**
 * @author Joram Barrez
 */
public class FormPopupWindow extends Window {

  protected static final long serialVersionUID = -1754225937375971709L;

  protected static final String TITLE = "Define form";
  protected static final String FORM_TITLE = "Form title";
  protected static final String DESCRIPTION = "Description";
  protected static final String DATA = "Data";

  protected Object taskItemId;
  protected TaskFormModel formModel;

  protected TextField titleField;
  protected TextField descriptionField;
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
    GridLayout layout = new GridLayout(2, 4);
    layout.setSpacing(true);
    addComponent(layout);

    // Title
    layout.addComponent(new Label(FORM_TITLE));
    titleField = new TextField();
    layout.addComponent(titleField);

    // Description
    layout.addComponent(new Label(DESCRIPTION));
    descriptionField = new TextField();
    descriptionField.setRows(2);
    descriptionField.setColumns(25);
    layout.addComponent(descriptionField);

    // Property table
    propertyTable = new PropertyTable();
    layout.addComponent(new Label(DATA));
    layout.addComponent(propertyTable);
    fillFormFields();

    // Buttons
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.setSpacing(true);

    // Save button
    Button saveButton = new Button("Save");
    buttons.addComponent(saveButton);
    saveButton.addListener(new Button.ClickListener() {

      private static final long serialVersionUID = -2906886872414089331L;

      public void buttonClick(ClickEvent event) {
        FormDto form = createForm();
        formModel.addForm(taskItemId, form);
        close();
      }
    });

    // Delete button
    Button deleteButton = new Button("Delete");
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

  public FormDto createForm() {
    FormDto formDto = new FormDto();
    formDto.setTitle((String) titleField.getValue());
    formDto.setDescription((String) descriptionField.getValue());
    for (Object itemId : propertyTable.getItemIds()) {
      FormPropertyDto formProperty = new FormPropertyDto();
      formProperty.setProperty((String) propertyTable.getItem(itemId).getItemProperty("property").getValue());
      formProperty.setType((String) ((ComboBox) propertyTable.getItem(itemId).getItemProperty("type").getValue()).getValue());
      formProperty.setRequired((Boolean) ((CheckBox) propertyTable.getItem(itemId).getItemProperty("required").getValue()).getValue());
      formDto.addFormProperty(formProperty);
    }
    return formDto;
  }

  protected void fillFormFields() {
    FormDto form = formModel.getForm(taskItemId);
    if (form == null) {
      propertyTable.addPropertyRow();
    } else {
      titleField.setValue(form.getTitle());
      descriptionField.setValue(form.getDescription());
      for (FormPropertyDto property : form.getFormProperties()) {
        propertyTable.addPropertyRow(property.getProperty(), property.getType(), property.isRequired());
      }
    }
  }

}

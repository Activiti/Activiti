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
package org.activiti.kickstart.ui.panel;

import org.activiti.kickstart.dto.AdhocWorkflowDto;
import org.activiti.kickstart.dto.TaskDto;
import org.activiti.kickstart.service.AdhocWorkflowService;
import org.activiti.kickstart.service.ServiceLocator;
import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.popup.ErrorPopupWindow;
import org.activiti.kickstart.ui.popup.ProcessImagePopupWindow;
import org.activiti.kickstart.ui.table.TaskTable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class AdhocWorkflowPanel extends Panel {

  protected static final long serialVersionUID = -2074647293591779784L;

  protected static final String NEW_ADHOC_WORKFLOW_TITLE = "Create new adhoc workflow";
  protected static final String EXISTING_ADHOC_WORKFLOW_TITLE = "Edit workflow";
  protected static final String NAME_FIELD = "Name";
  protected static final String DESCRIPTION_FIELD = "Description";

  // ui
  protected Label titleLabel;
  protected TextField nameField;
  protected TextField descriptionField;
  protected TaskTable taskTable;
  protected Resource saveImage;
  protected Resource generateImageImage;

  // dependencies
  protected ViewManager viewManager;
  protected AdhocWorkflowService adhocWorkflowService;
  protected AdhocWorkflowDto existingAdhocWorkflow;

  public AdhocWorkflowPanel(ViewManager viewManager, AdhocWorkflowDto existingAdhocWorkflow) {
    this.viewManager = viewManager;
    this.existingAdhocWorkflow = existingAdhocWorkflow;
    this.saveImage = new ClassResource("images/page_save.png", viewManager.getApplication());
    this.generateImageImage = new ClassResource("images/image.png", viewManager.getApplication());
    init();
  }

  public AdhocWorkflowPanel(ViewManager viewManager) {
    this(viewManager, null);
  }

  protected void init() {
    setSizeFull();
    setStyleName(Reindeer.PANEL_LIGHT);
    this.adhocWorkflowService = ServiceLocator.getAdhocWorkflowService();
    initUi();
  }

  protected void initUi() {
    initTitle();

    GridLayout layout = new GridLayout(2, 7);
    layout.setSpacing(true);
    addComponent(layout);

    initNameField(layout);
    initDescriptionField(layout);
    initTaskTable(layout);
    initButtons(layout);
  }

  protected void initTitle() {
    VerticalLayout verticalLayout = new VerticalLayout();
    if (existingAdhocWorkflow == null) {
      titleLabel = new Label(NEW_ADHOC_WORKFLOW_TITLE);
    } else {
      titleLabel = new Label(EXISTING_ADHOC_WORKFLOW_TITLE + " '" + existingAdhocWorkflow.getName() + "'");
    }
    titleLabel.setStyleName(Reindeer.LABEL_H1);
    verticalLayout.addComponent(titleLabel);

    // add some empty space
    Label emptyLabel = new Label("");
    emptyLabel.setHeight("1.5em");
    verticalLayout.addComponent(emptyLabel);

    addComponent(verticalLayout);
  }

  protected void initNameField(GridLayout layout) {
    nameField = new TextField();
    nameField.setWriteThrough(true);
    nameField.setImmediate(true);
    if (existingAdhocWorkflow != null) {
      nameField.setValue(existingAdhocWorkflow.getName());
    }
    
    layout.addComponent(new Label(NAME_FIELD));
    layout.addComponent(nameField);
  }

  protected void initDescriptionField(GridLayout layout) {
    descriptionField = new TextField();
    descriptionField.setRows(4);
    descriptionField.setColumns(35);
    if (existingAdhocWorkflow != null) {
      descriptionField.setValue(existingAdhocWorkflow.getDescription());
    }
    layout.addComponent(new Label(DESCRIPTION_FIELD));
    layout.addComponent(descriptionField);
  }

  protected void initTaskTable(GridLayout layout) {
    taskTable = new TaskTable(viewManager);
    if (existingAdhocWorkflow == null) {
      taskTable.addDefaultTaskRow();
    } else {
      for (TaskDto task : existingAdhocWorkflow.getTasks()) {
        taskTable.addTaskRow(task);
      }
    }

    layout.addComponent(new Label("Tasks"));
    layout.addComponent(taskTable);
  }

  protected void initButtons(GridLayout layout) {
    final Button saveButton = new Button("Save");
    saveButton.setEnabled(nameField.getValue() != null && !"".equals((String) nameField.getValue()));
    saveButton.setIcon(saveImage);
    saveButton.addListener(new Button.ClickListener() {

      private static final long serialVersionUID = 3546324122090420533L;

      public void buttonClick(ClickEvent event) {
        try {
          adhocWorkflowService.deployAdhocWorkflow(createAdhocWorkflow());
          Panel successPanel = new Panel();
          successPanel.setStyleName(Reindeer.PANEL_LIGHT);
          Label successLabel = new Label("Process successfully deployed");
          successPanel.addComponent(successLabel);
          viewManager.switchWorkArea(ViewManager.PROCESS_SUCESSFULLY_DEPLOYED, successPanel);
        } catch (Exception e) {
          e.printStackTrace();
          viewManager.showPopupWindow(new ErrorPopupWindow(e));
        }
      }
    });
    
    // Dependending on namefield value, save button is enabled
    nameField.addListener(new ValueChangeListener() {
        private static final long serialVersionUID = -4357300368046546003L;
        public void valueChange(ValueChangeEvent event) {
          if (nameField.getValue() != null 
                  && !"".equals((String) nameField.getValue())) {
            saveButton.setEnabled(true);
          } else {
            saveButton.setEnabled(false);
          }
        }
      });

    Button generateImageButton = new Button("View image");
    generateImageButton.setIcon(generateImageImage);
    generateImageButton.addListener(new Button.ClickListener() {

      private static final long serialVersionUID = 5671158538486627690L;

      public void buttonClick(ClickEvent event) {
        viewManager.showPopupWindow(new ProcessImagePopupWindow(viewManager, createAdhocWorkflow()));
      }

    });

    HorizontalLayout footer = new HorizontalLayout();
    footer.setSpacing(true);
    footer.addComponent(saveButton);
    footer.addComponent(generateImageButton);
    layout.addComponent(new Label());
    layout.addComponent(footer);
  }

  protected AdhocWorkflowDto createAdhocWorkflow() {
    AdhocWorkflowDto adhocWorkflow = new AdhocWorkflowDto();
    adhocWorkflow.setName((String) nameField.getValue());
    adhocWorkflow.setDescription((String) descriptionField.getValue());
    for (TaskDto task : taskTable.getTasks()) {
      adhocWorkflow.addTask(task);
    }
    return adhocWorkflow;
  }

}

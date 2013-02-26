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

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
public class DescriptionComponent extends CssLayout {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  protected I18nManager i18nManager;
  protected transient TaskService taskService;
  
  protected Label descriptionLabel;
  protected VerticalLayout editLayout;
  
  public DescriptionComponent(Task task, I18nManager i18nManager, TaskService taskService) {
    this.task = task;
    this.i18nManager = i18nManager;
    this.taskService = taskService;
    
    setWidth(100, UNITS_PERCENTAGE);
    initDescriptionLabel();
    initEditLayout();
    initLayoutClickListener();
  }
  
  protected void initDescriptionLabel() {
    String descriptionText = null;
    if (task.getDescription() != null && !"".equals(task.getDescription())) {
      descriptionText = task.getDescription();
    } else {
      descriptionText = i18nManager.getMessage(Messages.TASK_NO_DESCRIPTION);
    }
    descriptionLabel = new Label(descriptionText);
    descriptionLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    addComponent(descriptionLabel);
  }
  
  protected void initEditLayout() {
    editLayout = new VerticalLayout();
    editLayout.setSpacing(true);
  }
  
  protected void initLayoutClickListener() {
    addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        if (event.getClickedComponent() != null && event.getClickedComponent().equals(descriptionLabel)) {
          // textarea
          final TextArea descriptionTextArea = new TextArea();
          descriptionTextArea.setWidth(100, UNITS_PERCENTAGE);
          descriptionTextArea.setValue(task.getDescription());
          editLayout.addComponent(descriptionTextArea);
          
          // ok button
          Button okButton = new Button(i18nManager.getMessage(Messages.BUTTON_OK));
          editLayout.addComponent(okButton);
          editLayout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);
          
          // replace
          replaceComponent(descriptionLabel, editLayout);
          
          // When OK is clicked -> update task data + ui
          okButton.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
              // Update data
              task.setDescription(descriptionTextArea.getValue().toString());
              taskService.saveTask(task);
              
              // Update UI
              descriptionLabel.setValue(task.getDescription());
              replaceComponent(editLayout, descriptionLabel);
            }
          });
        }
      }
    });
  }

}

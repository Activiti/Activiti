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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.CssLayout;


/**
 * @author Joram Barrez
 */
public class PriorityComponent extends CssLayout {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  protected I18nManager i18nManager;
  protected transient TaskService taskService;
  
  protected PriorityLabel priorityLabel;
  protected PriorityComboBox priorityComboBox;
  
  public PriorityComponent(Task task, I18nManager i18nManager, TaskService taskService) {
    this.task = task;
    this.i18nManager = i18nManager;
    this.taskService = taskService;
    
    setSizeUndefined();
    initPriorityLabel();
    initPriorityComboBox();
    initLayoutClickListener();
    initComboBoxListener();
  }
  
  protected void initPriorityLabel() {
    priorityLabel = new PriorityLabel(task, i18nManager);
    addComponent(priorityLabel);
  }
  
  protected void initPriorityComboBox() {
    priorityComboBox = new PriorityComboBox(i18nManager, priorityLabel.getValue());
  }
  
  protected void initLayoutClickListener() {
    addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        if (event.getClickedComponent() != null && event.getClickedComponent().equals(priorityLabel)) {
          // Replace label with combobox
          replaceComponent(priorityLabel, priorityComboBox);
        }
      }
    });
  }
  
  protected void initComboBoxListener() {
    priorityComboBox.addListener(new ValueChangeListener() {
      public void valueChange(ValueChangeEvent event) {
        // save new priority
        task.setPriority(priorityComboBox.getPriority());
        taskService.saveTask(task);
        
        // Replace again with label
        priorityLabel.setValue(task.getPriority());
        replaceComponent(priorityComboBox, priorityLabel);
      }
    });
  }

}

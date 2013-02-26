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

import java.util.Date;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;


/**
 * @author Joram Barrez
 */
public class DueDateComponent extends CssLayout {
  
  
  private static final long serialVersionUID = 1L;
  protected Task task;
  protected I18nManager i18nManager;
  protected transient TaskService taskService;
  
  protected Label dueDateLabel;
  protected DateField dueDateField;

  public DueDateComponent(final Task task, final I18nManager i18nManager, final TaskService taskService) {
    this.task = task;
    this.i18nManager = i18nManager;
    this.taskService = taskService;
    
    setSizeUndefined();
    initDueDateLabel();
    initDueDateField();
    initLayoutClickListener();
    initDueDateFieldListener();
  }
  
  protected void initDueDateLabel() {
    dueDateLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.TASK_DUEDATE_SHORT),
            task.getDueDate(), i18nManager.getMessage(Messages.TASK_DUEDATE_UNKNOWN), false);
    dueDateLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_DUEDATE);
    dueDateLabel.setSizeUndefined();
    dueDateLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    addComponent(dueDateLabel);
  }
  
  protected void initDueDateField() {
    dueDateField = new DateField();
    if (task.getDueDate() != null) {
      dueDateField.setValue(task.getDueDate());
    } else {
      dueDateField.setValue(new Date());
    }
    dueDateField.setWidth(125, UNITS_PIXELS);
    dueDateField.setResolution(DateField.RESOLUTION_DAY);
    dueDateField.setImmediate(true);
  }
  
  protected void initLayoutClickListener() {
    addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        if (event.getClickedComponent() != null && event.getClickedComponent().equals(dueDateLabel)) { 
          // replace label with textfield
          replaceComponent(dueDateLabel, dueDateField);
        }
      }
    });
  }
  
  protected void initDueDateFieldListener() {
    dueDateField.addListener(new ValueChangeListener() {
      public void valueChange(ValueChangeEvent event) {
        if (dueDateField.getValue() != null) {
          // save new duedate
          task.setDueDate((Date) dueDateField.getValue());
          taskService.saveTask(task);
          
          // replace with new label
          dueDateLabel.setValue(task.getDueDate());
          replaceComponent(dueDateField, dueDateLabel);
        }
      }
    });
  }
  
}

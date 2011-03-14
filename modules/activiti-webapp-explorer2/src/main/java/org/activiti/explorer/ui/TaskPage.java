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

package org.activiti.explorer.ui;

import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.Constants;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskPage extends CustomComponent {
  
  private static final long serialVersionUID = 2310017323549425167L;
  
  // services
  protected TaskService taskService;
  
  // ui
  protected ViewManager viewManager;
  protected VerticalLayout mainLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  protected Table taskTable;
  
  public TaskPage(ViewManager viewManager) {
    this.viewManager = viewManager;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    mainLayout = new VerticalLayout();
    mainLayout.setSizeFull();
    setCompositionRoot(mainLayout);
    setSizeFull();
    
    initTaskMenuBar();
    initMainSplitPanel();
    initTaskList();
  }

  protected void initMainSplitPanel() {
    // The actual content of the page is a HorizontalSplitPanel,
    // with on the left side the task list
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(20, HorizontalSplitPanel.UNITS_PERCENTAGE);
    mainLayout.addComponent(mainSplitPanel);
    mainLayout.setExpandRatio(mainSplitPanel, 1.0f);
  }
  
  protected void initTaskMenuBar() {
    TaskMenuBar taskMenuBar = new TaskMenuBar(viewManager);
    mainLayout.addComponent(taskMenuBar);
  }
  
  protected void initTaskList() {
    this.taskTable = new Table();
    taskTable.addStyleName(Constants.STYLE_TASK_LIST);
    
    // Set non-editable, selectable and full-size
    taskTable.setEditable(false);
    taskTable.setImmediate(true);
    taskTable.setSelectable(true);
    taskTable.setNullSelectionAllowed(false);
    taskTable.setSizeFull();
            
    // Create column header
    taskTable.addContainerProperty("task", TaskListEntry.class, null);
    taskTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    // Listener to change right panel when clicked on a task
    taskTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Integer id = (Integer) event.getProperty().getValue();
        mainSplitPanel.setSecondComponent(new Label("task " + id));
      }
    });
    
    // Populate list with tasks
    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    for (Task task : tasks) {
      addTaskListEntry(task.getId(), task.getName());
    }
    
    mainSplitPanel.setFirstComponent(taskTable);
  }
  
  protected void addTaskListEntry(String taskId, String name) {
    TaskListEntry entry = new TaskListEntry(viewManager, taskTable, name, taskId);
    taskTable.addItem(new Object[] {entry}, taskId);
  }

}

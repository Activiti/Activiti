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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


/**
 * Abstract super class for all task pages (inbox, queued, archived, etc.),
 * Builds up the default UI: task list on the left, central panel and events on the right.
 * 
 * @author Joram Barrez
 */
public abstract class TaskPage extends AbstractPage {
  
  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected TaskService taskService;
  protected LazyLoadingContainer taskListContainer;
  protected LazyLoadingQuery lazyLoadingQuery;
  protected TaskEventsPanel taskEventPanel;
  
  
  public TaskPage() {
    taskService =  ProcessEngines.getDefaultProcessEngine().getTaskService();
  }
  
  public TaskPage(String taskId) {
    this();
    this.taskId = taskId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    if (taskId == null) {
      selectListElement(0);
    } else {
      int index = taskListContainer.getIndexForObjectId(taskId);
      selectListElement(index);
    }
  }
  
  @Override
  protected ToolBar createMenuBar() {
    return new TaskMenuBar();
  }
  
  @Override
  protected Table createList() {
    final Table taskTable = new Table();
    taskTable.addStyleName(ExplorerLayout.STYLE_TASK_LIST);
    taskTable.addStyleName(ExplorerLayout.STYLE_SCROLLABLE);
    
    // Listener to change right panel when clicked on a task
    taskTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = taskTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        
        if(item != null) {
          String taskId = (String) item.getItemProperty("id").getValue();
          Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
          setDetailComponent(new TaskDetailPanel(task, TaskPage.this));
          taskEventPanel.setTask(task);
          
          UriFragment taskFragment = getUriFragment(taskId);
          ExplorerApp.get().setCurrentUriFragment(taskFragment);
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(getUriFragment(null));
        }
      }
    });
    
    this.lazyLoadingQuery = createLazyLoadingQuery();
    this.taskListContainer = new LazyLoadingContainer(lazyLoadingQuery, 10);
    taskTable.setContainerDataSource(taskListContainer);
    
    // Create column header
    taskTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.TASK));
    taskTable.setColumnWidth("icon", 22);
    
    taskTable.addContainerProperty("name", String.class, null);
    taskTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return taskTable;
  }
  
  @Override
  protected Component getEventComponent() {
    return getTaskEventPanel();
  }
  
  public TaskEventsPanel getTaskEventPanel() {
    if(taskEventPanel == null) {
      taskEventPanel = new TaskEventsPanel(null);
    }
    return taskEventPanel;
  }
  
  protected abstract LazyLoadingQuery createLazyLoadingQuery();
  
  protected abstract UriFragment getUriFragment(String taskId);
  
}

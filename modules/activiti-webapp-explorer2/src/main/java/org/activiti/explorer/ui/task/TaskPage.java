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
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Images;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.TaskNavigationHandler;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public abstract class TaskPage extends AbstractPage {
  
  private static final long serialVersionUID = 1L;
  
  protected TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
  
  protected LazyLoadingContainer taskListContainer;
  protected LazyLoadingQuery lazyLoadingQuery;
  
  @Override
  protected Component createMenuBar() {
    return new TaskMenuBar();
  }
  
  @Override
  protected Table createList() {
    final Table taskTable = new Table();
    taskTable.addStyleName(Constants.STYLE_TASK_LIST);
    
    // Listener to change right panel when clicked on a task
    taskTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = taskTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String taskId = (String) item.getItemProperty("id").getValue();
          splitPanel.setSecondComponent(new TaskDetailPanel(taskId, TaskPage.this));
          
          UriFragment taskFragment = new UriFragment(TaskNavigationHandler.TASK_URI_PART, taskId);
          taskFragment.addParameter("category", TaskNavigationHandler.CATEGORY_INBOX);
          ExplorerApplication.getCurrent().setCurrentUriFragment(taskFragment);
        } else {
          // Nothing is selected
          splitPanel.removeComponent(splitPanel.getSecondComponent());
          ExplorerApplication.getCurrent().setCurrentUriFragment(new UriFragment(TaskNavigationHandler.TASK_URI_PART));
        }
      }
    });
    
    this.lazyLoadingQuery = createLazyLoadingQuery();
    this.taskListContainer = new LazyLoadingContainer(lazyLoadingQuery, 10);
    taskTable.setContainerDataSource(taskListContainer);
    
    // Create column header
    taskTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.TASK));
    taskTable.setColumnWidth("icon", 32);
    
    taskTable.addContainerProperty("name", String.class, null);
    taskTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return taskTable;
  }
  
  protected abstract LazyLoadingQuery createLazyLoadingQuery();
  
}

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
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public abstract class TaskPage extends CustomComponent {
  
  private static final long serialVersionUID = 2310017323549425167L;
  
  // services
  protected TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
  
  // ui
  protected VerticalLayout taskPageLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  protected Table taskTable;
  protected LazyLoadingContainer taskListContainer;
  protected LazyLoadingQuery lazyLoadingQuery;
  
  public TaskPage(LazyLoadingQuery lazyLoadingQuery) {
    this.lazyLoadingQuery = lazyLoadingQuery;
    initUi();
  }
  
  protected void initUi() {
    addTaskPageLayout();
    addTaskMenuBar();
    addMainSplitPanel();
    addTaskList();
  }
  

  protected void addTaskPageLayout() {
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    taskPageLayout = new VerticalLayout();
    taskPageLayout.setSizeFull();
    setCompositionRoot(taskPageLayout);
  }

  protected void addMainSplitPanel() {
    // The actual content of the page is a HorizontalSplitPanel,
    // with on the left side the task list
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(17, HorizontalSplitPanel.UNITS_PERCENTAGE);
    taskPageLayout.addComponent(mainSplitPanel);
    taskPageLayout.setExpandRatio(mainSplitPanel, 1.0f);
  }
  
  protected void addTaskMenuBar() {
    TaskMenuBar taskMenuBar = new TaskMenuBar();
    taskPageLayout.addComponent(taskMenuBar);
  }
  
  protected void addTaskList() {
    this.taskTable = new Table();
    taskTable.addStyleName(Constants.STYLE_TASK_LIST);
    
    
    // Set non-editable, selectable and full-size
    taskTable.setEditable(false);
    taskTable.setImmediate(true);
    taskTable.setSelectable(true);
    taskTable.setNullSelectionAllowed(false);
    taskTable.setSortDisabled(true);
    taskTable.setSizeFull();
            
    // Listener to change right panel when clicked on a task
    taskTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = taskTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String taskId = (String) item.getItemProperty("id").getValue();
          mainSplitPanel.setSecondComponent(new TaskDetailPanel(taskId, TaskPage.this));
          
          UriFragment taskFragment = new UriFragment(TaskNavigationHandler.TASK_URI_PART, taskId);
          taskFragment.addParameter("category", TaskNavigationHandler.CATEGORY_INBOX);
          
          ExplorerApplication.getCurrent().setCurrentUriFragment(taskFragment);
        } else {
          // Nothing is selected
          mainSplitPanel.removeComponent(mainSplitPanel.getSecondComponent());
          ExplorerApplication.getCurrent().setCurrentUriFragment(new UriFragment(TaskNavigationHandler.TASK_URI_PART));
        }
      }
    });
    
    this.taskListContainer = new LazyLoadingContainer(lazyLoadingQuery, 10);
    taskTable.setContainerDataSource(taskListContainer);
    
    // Create column header
    taskTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.TASK));
    taskTable.setColumnWidth("icon", 32);
    
    taskTable.addContainerProperty("name", String.class, null);
    taskTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    mainSplitPanel.setFirstComponent(taskTable);
  }
  
  /**
   * Selects an entry by its internal container index in the task list.
   */
  public void selectTask(int index) {
    if (taskTable.getContainerDataSource().size() > index) {
      taskTable.select(index);
      taskTable.setCurrentPageFirstItemId(index);
    }
  }
  
  /**
   * Clears the current item cache and refreshes
   * the currently visible items in the task table.
   */
  public void refreshCurrentTasks() {
    Integer pageIndex = (Integer) taskTable.getCurrentPageFirstItemId();
    Integer selectedIndex = (Integer) taskTable.getValue();
    taskTable.removeAllItems();
    
    // Remove all items
    taskListContainer.removeAllItems();
    
    // Try to select the next one in the list
    Integer max = taskTable.getContainerDataSource().size();
    if(pageIndex > max) {
      pageIndex = max -1;
    }
    if(selectedIndex > max) {
      selectedIndex = max -1;
    }
    taskTable.setCurrentPageFirstItemIndex(pageIndex);
    selectTask(selectedIndex);
  }
  
}

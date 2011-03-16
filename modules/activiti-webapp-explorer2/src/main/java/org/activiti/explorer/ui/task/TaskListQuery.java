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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ui.ViewManager;
import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class TaskListQuery extends AbstractBeanQuery<TaskListEntry> {
  
  protected ViewManager viewManager;
  protected TaskService taskService;
  protected Table taskTable;
  
  public TaskListQuery(QueryDefinition definition, Map<String, Object> queryConfiguration, 
          Object[] sortPropertyIds, boolean[] sortStates) {
    super(definition, queryConfiguration, sortPropertyIds, sortStates);
    this.taskService = (TaskService) getQueryConfiguration().get("taskService");
    this.viewManager = (ViewManager) getQueryConfiguration().get("viewManager");
    this.taskTable = (Table) getQueryConfiguration().get("taskTable");
  }

  protected List<TaskListEntry> loadBeans(int startIndex, int count) {
    System.out.println("Loading task list entries from " + startIndex + " count=" + count);
    List<Task> tasks = taskService.createTaskQuery().listPage(startIndex, count);
    return convert(tasks, startIndex, count);
  }
  
  protected List<TaskListEntry> convert(List<Task> tasks, int startIndex, int count) {
    List<TaskListEntry> taskListEntries = new ArrayList<TaskListEntry>();
    for (int i=0; i<tasks.size(); i++) {
      taskListEntries.add(new TaskListEntry(viewManager, taskTable, startIndex + i, tasks.get(i))); // item id is the actual numerical index in the container
    }
    return taskListEntries;
  }

  public int size() {
    return (int) taskService.createTaskQuery().count();
  }
  
  protected TaskListEntry constructBean() {
    throw new UnsupportedOperationException();
  }
  
  protected void saveBeans(List<TaskListEntry> addedTasks, List<TaskListEntry> modifiedTasks, List<TaskListEntry> removedTasks) {
    throw new UnsupportedOperationException();
  }

}

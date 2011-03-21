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

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.data.LazyLoadingContainer;

import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class TaskQuery extends AbstractLazyLoadingQuery<TaskListEntry> {
  
  protected TaskService taskService;
  protected Table taskTable;
  protected LazyLoadingContainer lazyLoadingContainer;
  
  public TaskQuery(TaskService taskService, Table taskTable) {
    this.taskService = taskService;
    this.taskTable = taskTable;
  }

  public int size() {
    return (int) taskService.createTaskQuery().count();
  }
  
  public List<TaskListEntry> loadBeans(int start, int count) {
    List<Task> tasks = taskService.createTaskQuery().listPage(start, count);

    List<TaskListEntry> taskListEntries = new ArrayList<TaskListEntry>();
    int startIndex = start;
    for (int i=0; i<tasks.size(); i++) {
      taskListEntries.add(new TaskListEntry(taskTable, startIndex + i, tasks.get(i))); // item id is the actual numerical index in the container
    }
    return taskListEntries;
  }
  
}

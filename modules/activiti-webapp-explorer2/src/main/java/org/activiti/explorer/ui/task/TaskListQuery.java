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
import org.activiti.engine.task.TaskQuery;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.data.LazyLoadingContainer;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class TaskListQuery extends AbstractLazyLoadingQuery {
  
  protected TaskService taskService;
  protected LazyLoadingContainer lazyLoadingContainer;
  
  public TaskListQuery(TaskService taskService, Table taskTable) {
    this.taskService = taskService;
  }

  public int size() {
    return (int) taskService.createTaskQuery().count();
  }
  
  public List<Item> loadItems(int start, int count) {
    List<Task> tasks = createBaseQuery().listPage(start, count);
    List<Item> items = new ArrayList<Item>();
    for (Task task : tasks) {
      items.add(createTaskListItem(task));
    }
    return items;
  }
  
  public Item loadSingleResult(String id) {
    return createTaskListItem(createBaseQuery().taskId(id).singleResult());
  }
  
  protected TaskListItem createTaskListItem(Task task) {
    TaskListItem taskListItem = new TaskListItem();
    taskListItem.addItemProperty("id", new ObjectProperty<String>(task.getId()));
    taskListItem.addItemProperty("name", new ObjectProperty<String>(task.getName()));
    return taskListItem;
  }
  
  protected TaskQuery createBaseQuery() {
    return taskService.createTaskQuery().orderByTaskId().asc();
  }
  
  public void setSorting(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  
  class TaskListItem extends PropertysetItem implements Comparable<TaskListItem>{

    private static final long serialVersionUID = 1L;

    public int compareTo(TaskListItem other) {
      String taskId = (String) getItemProperty("id").getValue();
      String otherTaskId = (String) other.getItemProperty("id").getValue();
      return taskId.compareTo(otherTaskId);
    }
    
  }
  
}

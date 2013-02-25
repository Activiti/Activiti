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
package org.activiti.explorer.ui.task.data;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Joram Barrez
 */
public abstract class AbstractTaskListQuery extends AbstractLazyLoadingQuery {
  
  protected String userId;
  protected transient TaskService taskService;
  
  public AbstractTaskListQuery() {
    this.userId = ExplorerApp.get().getLoggedInUser().getId();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
  }

  public int size() {
    return (int) getQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<Task> tasks = getQuery().listPage(start, count);
    List<Item> items = new ArrayList<Item>();
    for (Task task : tasks) {
      items.add(new TaskListItem(task));
    }
    return items;
  }

  public Item loadSingleResult(String id) {
    Task task = getQuery().taskId(id).singleResult();
    if(task != null) {
      return new TaskListItem(task);
    }
    return null;
  }

  public void setSorting(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  protected abstract TaskQuery getQuery();
  
}

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

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.data.LazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * {@link LazyLoadingQuery} for the Archived tasks page.
 * 
 * @author Joram Barrez
 */
public class ArchivedListQuery extends AbstractLazyLoadingQuery {

  protected String userId;
  protected transient HistoryService historyService;
  
  public ArchivedListQuery() {
    this.userId = ExplorerApp.get().getLoggedInUser().getId();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
  }

  public int size() {
    return (int) createQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<HistoricTaskInstance> historicTaskInstances = createQuery().listPage(start, count);
    List<Item> items = new ArrayList<Item>();
    for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
      items.add(new TaskListItem(historicTaskInstance));
    }
    return items;
  }

  public Item loadSingleResult(String id) {
    return new TaskListItem(createQuery().taskId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  protected HistoricTaskInstanceQuery createQuery() {
    return historyService.createHistoricTaskInstanceQuery().taskOwner(userId).finished();
  }
  
}

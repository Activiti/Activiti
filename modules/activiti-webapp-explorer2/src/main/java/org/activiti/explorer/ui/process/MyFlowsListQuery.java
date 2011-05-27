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

package org.activiti.explorer.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Frederik Heremans
 */
public class MyFlowsListQuery extends AbstractLazyLoadingQuery {
  
  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  
  protected Map<String, ProcessDefinition> cachedProcessDefinitions;
  
  public MyFlowsListQuery(HistoryService historyService, RepositoryService repositoryService) {
    this.historyService = historyService;
    this.repositoryService = repositoryService;
    cachedProcessDefinitions = new HashMap<String, ProcessDefinition>();
  }
  
  public List<Item> loadItems(int start, int count) {
    List<HistoricProcessInstance> processInstances = historyService
      .createHistoricProcessInstanceQuery()
      .startedBy(ExplorerApp.get().getLoggedInUser().getId())
      .unfinished()
      .list();
    
    List<Item> items = new ArrayList<Item>();
    for (HistoricProcessInstance processInstance : processInstances) {
      items.add(createItem(processInstance));
    }
    return items;
  }
  
  public Item loadSingleResult(String id) {
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
      .startedBy(ExplorerApp.get().getLoggedInUser().getId())
      .unfinished()
      .processInstanceId(id).singleResult();
    if (processInstance != null) {
      return createItem(processInstance);
    }
    return null;
  }
  
  protected ProcessInstanceListItem createItem(HistoricProcessInstance processInstance) {
    ProcessInstanceListItem item = new ProcessInstanceListItem();
    item.addItemProperty("id", new ObjectProperty<String>(processInstance.getId()));

    ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
    
    String itemName = processDefinition.getName() + " (" + processInstance.getId() + ")";
    item.addItemProperty("name", new ObjectProperty<String>(itemName));
    return item;
  }

  protected ProcessDefinition getProcessDefinition(String id) {
    ProcessDefinition processDefinition = cachedProcessDefinitions.get(id);
    if(processDefinition == null) {
      processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
      cachedProcessDefinitions.put(id, processDefinition);
    }
    return processDefinition;
  }

  public int size() {
    return (int) historyService.createHistoricProcessInstanceQuery()
    .startedBy(ExplorerApp.get().getLoggedInUser().getId())
    .unfinished()
    .count();
  }

  public void setSorting(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class ProcessInstanceListItem extends PropertysetItem implements Comparable<ProcessInstanceListItem>{

    private static final long serialVersionUID = 1L;

    public int compareTo(ProcessInstanceListItem other) {
      // process instances are ordered by id
      String id = (String) getItemProperty("id").getValue();
      String otherId = (String) other.getItemProperty("id").getValue();
      return id.compareTo(otherId);
    }
  }
  
}

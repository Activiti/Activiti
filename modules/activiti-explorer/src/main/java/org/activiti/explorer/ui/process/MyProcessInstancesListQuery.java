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


/**
 * @author Frederik Heremans
 */
public class MyProcessInstancesListQuery extends AbstractLazyLoadingQuery {
  
  protected transient HistoryService historyService;
  protected transient RepositoryService repositoryService;
  
  protected Map<String, ProcessDefinition> cachedProcessDefinitions;
  
  public MyProcessInstancesListQuery(HistoryService historyService, RepositoryService repositoryService) {
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
  
  protected ProcessInstanceItem createItem(HistoricProcessInstance processInstance) {
    ProcessInstanceItem item = new ProcessInstanceItem();
    item.addItemProperty("id", new ObjectProperty<String>(processInstance.getId(), String.class));

    ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionId());
    
    String itemName = getProcessDisplayName(processDefinition) + " (" + processInstance.getId() + ")"
      + (processInstance.getBusinessKey() != null? processInstance.getBusinessKey() : "");
    item.addItemProperty("name", new ObjectProperty<String>(itemName, String.class));
    return item;
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
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
  
}

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

package org.activiti.explorer.ui.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Frederik Heremans
 */
public class MyFlowsListQuery extends AbstractLazyLoadingQuery {
  
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  
  protected Map<String, ProcessDefinition> cachedProcessDefinitions;
  
  public MyFlowsListQuery(RuntimeService runtimeService, RepositoryService repositoryService) {
    this.runtimeService = runtimeService;
    this.repositoryService = repositoryService;
    cachedProcessDefinitions = new HashMap<String, ProcessDefinition>();
  }
  
  public List<Item> loadItems(int start, int count) {
    List<ProcessInstance> processInstances = runtimeService
      .createProcessInstanceQuery()
      .list();
    
    List<Item> items = new ArrayList<Item>();
    for (ProcessInstance processInstance : processInstances) {
      items.add(createItem(processInstance));
    }
    return items;
  }
  
  public Item loadSingleResult(String id) {
    return createItem(runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult());
  }
  
  protected ProcessInstanceListItem createItem(ProcessInstance processInstance) {
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
    return (int) runtimeService.createProcessInstanceQuery().count();
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

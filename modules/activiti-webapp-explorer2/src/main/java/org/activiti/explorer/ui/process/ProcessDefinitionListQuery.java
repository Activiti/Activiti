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
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionListQuery extends AbstractLazyLoadingQuery {
  
  protected RepositoryService repositoryService;
  
  public ProcessDefinitionListQuery(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }
  
  public List<Item> loadItems(int start, int count) {
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .orderByProcessDefinitionName().asc()
      .orderByProcessDefinitionKey().asc() // name is not unique, so we add the order on key (so we can use it in the comparsion of ProcessDefinitionListItem)
      .listPage(start, count);
    
    List<Item> items = new ArrayList<Item>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      items.add(createItem(processDefinition));
    }
    return items;
  }
  
  public Item loadSingleResult(String id) {
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
    if (definition != null) {
      return createItem(definition);
    }
    return null;
  }
  
  protected ProcessDefinitionListItem createItem(ProcessDefinition processDefinition) {
    ProcessDefinitionListItem item = new ProcessDefinitionListItem();
    item.addItemProperty("id", new ObjectProperty<String>(processDefinition.getId(), String.class));
    item.addItemProperty("name", new ObjectProperty<String>(getProcessDisplayName(processDefinition), String.class));
    item.addItemProperty("key", new ObjectProperty<String>(processDefinition.getKey(), String.class));
    return item;
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }

  public int size() {
    return (int)repositoryService.createProcessDefinitionQuery().latestVersion().count();
  }

  public void setSorting(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  
  
  class ProcessDefinitionListItem extends PropertysetItem implements Comparable<ProcessDefinitionListItem>{

    private static final long serialVersionUID = 1L;

    public int compareTo(ProcessDefinitionListItem other) {
      // process definitions are ordered by name (see #loadItems in query)
      String name = (String) getItemProperty("name").getValue();
      String otherName = (String) other.getItemProperty("name").getValue();
      int comparison = name.compareTo(otherName);
      
      // Name is not unique for process definition
      // But the list is sorted on process definition key also, so we can use it to compare if the name is equal
      if (comparison != 0) {
        return comparison;
      } else {
        String key = (String) getItemProperty("key").getValue();
        String otherKey = (String) other.getItemProperty("key").getValue();
        return key.compareTo(otherKey);
      }
      
    }

  }
  
}

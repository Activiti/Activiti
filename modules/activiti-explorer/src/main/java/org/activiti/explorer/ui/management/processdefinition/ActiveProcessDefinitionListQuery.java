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

package org.activiti.explorer.ui.management.processdefinition;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;


/**
 * @author Joram Barrez
 */
public class ActiveProcessDefinitionListQuery extends AbstractLazyLoadingQuery {
  
  protected RepositoryService repositoryService;
  
  public ActiveProcessDefinitionListQuery() {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }

  public int size() {
    return (int) repositoryService.createProcessDefinitionQuery().active().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
            .active()
            .orderByProcessDefinitionName().asc()
            .orderByProcessDefinitionVersion().asc()
            .listPage(start, count);

    List<Item> processDefinitionItems = new ArrayList<Item>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      processDefinitionItems.add(new ActiveProcessDefinitionListItem(processDefinition));
    }
    
    return processDefinitionItems;
  }

  public Item loadSingleResult(String id) {
    return new ActiveProcessDefinitionListItem(repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  class ActiveProcessDefinitionListItem extends PropertysetItem implements Comparable<ActiveProcessDefinitionListItem> {
    
    private static final long serialVersionUID = 1L;
    
    public ActiveProcessDefinitionListItem(ProcessDefinition processDefinition) {
      addItemProperty("id", new ObjectProperty<String>(processDefinition.getId(), String.class));
      addItemProperty("key", new ObjectProperty<String>(processDefinition.getKey(), String.class));
      addItemProperty("name", new ObjectProperty<String>(processDefinition.getName() ,String.class));
      addItemProperty("version", new ObjectProperty<Integer>(processDefinition.getVersion() ,Integer.class));
    }
    
    public int compareTo(ActiveProcessDefinitionListItem other) {
      String name = (String) getItemProperty("name").getValue();
      String otherName = (String) other.getItemProperty("name").getValue();
      
      int comparison = name.compareTo(otherName);
      if (comparison != 0) {
        return comparison;
      } else {
        String key = (String) getItemProperty("key").getValue();
        String otherKey = (String) other.getItemProperty("key").getValue();
        comparison = key.compareTo(otherKey);
        
        if (comparison != 0) {
          return comparison;
        } else {
          Integer version = (Integer) getItemProperty("version").getValue();
          Integer otherVersion = (Integer) other.getItemProperty("version").getValue();
          return version.compareTo(otherVersion);
        }
      }
    }
    
  }
  
}

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


/**
 * @author Joram Barrez
 */
public class ActiveProcessDefinitionListQuery extends AbstractLazyLoadingQuery {
  
  protected transient RepositoryService repositoryService;
  
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
      processDefinitionItems.add(new ProcessDefinitionListItem(processDefinition));
    }
    
    return processDefinitionItems;
  }

  public Item loadSingleResult(String id) {
    return new ProcessDefinitionListItem(repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
}

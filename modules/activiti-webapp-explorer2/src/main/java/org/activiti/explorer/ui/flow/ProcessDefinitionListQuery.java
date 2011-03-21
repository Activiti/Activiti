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

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionListQuery extends AbstractLazyLoadingQuery<ProcessDefinition> {
  
  protected RepositoryService repositoryService;
  
  public ProcessDefinitionListQuery(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  protected List<ProcessDefinition> loadBeans(int startIndex, int count) {
    return repositoryService.createProcessDefinitionQuery()
      .latestVersion()
      .orderByProcessDefinitionName().asc()
      .listPage(startIndex, count);
  }

  public int size() {
    return (int)repositoryService.createProcessDefinitionQuery().latestVersion().count();
  }

  public int compareTo(Item searched, Item other) {
    // process definitions are ordered by name (see #loadBeans)
    String searchedName = (String) searched.getItemProperty("name").getValue();
    String otherName = (String) other.getItemProperty("name").getValue();
    return searchedName.compareTo(otherName);
  }

  protected ProcessDefinition loadBean(String id) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
  }
  
  public void setSorting(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
}

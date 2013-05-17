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

package org.activiti.explorer.ui.reports;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Joram Barrez
 */
public class ReportListQuery extends AbstractLazyLoadingQuery {
  
  private static final long serialVersionUID = -7865037930384885968L;

  private static final String REPORT_PROCESS_CATEGORY = "activiti-report";
  
  protected transient RepositoryService repositoryService;
  
  public ReportListQuery() {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }

  public int size() {
    return (int) createQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<ProcessDefinition> processDefinitions = createQuery().listPage(start, count);

    List<Item> reportItems = new ArrayList<Item>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      reportItems.add(new ReportListItem(processDefinition));
    }
    
    return reportItems;
  }
  
  protected ProcessDefinitionQuery createQuery() {
    return repositoryService.createProcessDefinitionQuery()
            .processDefinitionCategory(REPORT_PROCESS_CATEGORY)
            .latestVersion()
            .orderByProcessDefinitionName().asc();
  }

  public Item loadSingleResult(String id) {
    return new ReportListItem(repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
}

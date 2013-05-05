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

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Frederik Heremans
 */
public class SavedReportsListQuery extends AbstractLazyLoadingQuery {
  
  private static final long serialVersionUID = -7865037930384885968L;

  protected transient HistoryService historyService;
  
  public SavedReportsListQuery() {
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
  }

  public int size() {
    return (int) createQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<HistoricProcessInstance> processInstances = createQuery().listPage(start, count);

    List<Item> reportItems = new ArrayList<Item>();
    for (HistoricProcessInstance instance : processInstances) {
      reportItems.add(new SavedReportListItem(instance));
    }
    
    return reportItems;
  }
  
  protected HistoricProcessInstanceQuery createQuery() {
    // TODO: Add additional "processDefinitionCategory" on HistoricProcessInstanceQuery instead of
    // using variables to find all completed reports. This is more robust and performant
    return historyService.createHistoricProcessInstanceQuery()
           .finished()
           .startedBy(Authentication.getAuthenticatedUserId())
           .variableValueNotEquals("reportData", null);
  }

  public Item loadSingleResult(String id) {
    return new SavedReportListItem(historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
}

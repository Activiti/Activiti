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

package org.activiti.explorer.ui.alfresco;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Joram Barrez
 */
public class ProcessInstanceTableLazyQuery extends AbstractLazyLoadingQuery {
  
  protected RuntimeService runtimeService;
  protected String processDefinitionId;
  
  public ProcessInstanceTableLazyQuery() {
    this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
  }
  
  public ProcessInstanceTableLazyQuery(String processDefinitionId) {
    this();
    this.processDefinitionId = processDefinitionId;
  }

  public int size() {
    return (int) constructQuery().count();
  }
  
  public Item loadSingleResult(String id) {
    return new AlfrescoProcessInstanceTableItem(constructQuery().processInstanceId(id).singleResult());
  }
  
  public List<Item> loadItems(int start, int count) {
    List<ProcessInstance> processInstances = constructQuery().listPage(start, count);
    List<Item> items = new ArrayList<Item>(processInstances.size());
    for (ProcessInstance processInstance : processInstances) {
      items.add(new AlfrescoProcessInstanceTableItem(processInstance));
    }
    return items;
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  protected ProcessInstanceQuery constructQuery() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .orderByProcessInstanceId().asc();
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    return query;
  }
  
  public void setProcessDefintionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

}

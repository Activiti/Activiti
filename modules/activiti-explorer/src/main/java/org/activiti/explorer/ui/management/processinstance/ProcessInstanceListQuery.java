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

package org.activiti.explorer.ui.management.processinstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;

import com.vaadin.data.Item;


/**
 * @author Joram Barrez
 */
public class ProcessInstanceListQuery extends AbstractLazyLoadingQuery {
  
  protected transient RuntimeService runtimeService;
  protected transient RepositoryService repositoryService;
  
  protected Map<String, String> cachedProcessDefinitionNames = new HashMap<String, String>();
  
  public ProcessInstanceListQuery() {
    this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }

  public int size() {
    return (int) constructQuery().count();
  }

  public List<Item> loadItems(int start, int count) {
    List<ProcessInstance> processInstances = constructQuery().listPage(start, count);
    List<Item> items = new ArrayList<Item>();
    for (ProcessInstance processInstance : processInstances) {
      items.add(new ProcessInstanceListItem(processInstance, getProcessDefinitionName(processInstance.getProcessDefinitionId())));
    }
    return items;
  }

  public Item loadSingleResult(String id) {
    ProcessInstance processInstance = constructQuery().processInstanceId(id).singleResult();
    return new ProcessInstanceListItem(processInstance, processInstance.getProcessDefinitionId());
  }

  public void setSorting(Object[] propertyIds, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }

  protected ProcessInstanceQuery constructQuery() {
    return runtimeService.createProcessInstanceQuery()
      .orderByProcessInstanceId().asc();
  }
  
  protected String getProcessDefinitionName(String processDefinitionId) {
    if (!cachedProcessDefinitionNames.containsKey(processDefinitionId)) {
      ProcessDefinition definition =  repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId).singleResult();
      
      String name =definition.getName();
      if(name != null) {
        name = definition.getKey();
      }
      cachedProcessDefinitionNames.put(processDefinitionId, name);
    }
    return cachedProcessDefinitionNames.get(processDefinitionId);
  }

}

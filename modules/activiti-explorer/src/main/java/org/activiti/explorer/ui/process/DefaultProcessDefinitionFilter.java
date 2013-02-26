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

import java.io.Serializable;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.explorer.ui.process.ProcessDefinitionListQuery.ProcessDefinitionListItem;

import com.vaadin.data.util.ObjectProperty;

/**
 * @author Frederik Heremans
 */
public class DefaultProcessDefinitionFilter implements ProcessDefinitionFilter, Serializable {

  protected static final String PROPERTY_ID = "id";
  protected static final String PROPERTY_NAME = "name";
  protected static final String PROPERTY_KEY = "key";
  
  
  public ProcessDefinitionQuery getQuery(RepositoryService repositoryService) {
    return getBaseQuery(repositoryService)
            .orderByProcessDefinitionName().asc()
            .orderByProcessDefinitionKey().asc(); // name is not unique, so we add the order on key (so we can use it in the comparsion of ProcessDefinitionListItem)
  }
  
  public ProcessDefinitionQuery getCountQuery(RepositoryService repositoryService) {
    return getBaseQuery(repositoryService);
  }
  
  protected ProcessDefinitionQuery getBaseQuery(RepositoryService repositoryService) {
    return repositoryService
            .createProcessDefinitionQuery()
            .latestVersion()
            .active();
  }

  public ProcessDefinitionListItem createItem(ProcessDefinition processDefinition) {
    ProcessDefinitionListItem item = new ProcessDefinitionListItem();
    item.addItemProperty(PROPERTY_ID, new ObjectProperty<String>(processDefinition.getId()));
    item.addItemProperty(PROPERTY_NAME, new ObjectProperty<String>(getProcessDisplayName(processDefinition)));
    item.addItemProperty(PROPERTY_KEY, new ObjectProperty<String>(processDefinition.getKey()));
    return item;
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }

}

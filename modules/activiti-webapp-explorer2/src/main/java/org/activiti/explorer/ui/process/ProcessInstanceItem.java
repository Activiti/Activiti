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

import static org.activiti.explorer.ui.process.ProcessInstanceItem.PROPERTY_BUSINESS_KEY;
import static org.activiti.explorer.ui.process.ProcessInstanceItem.PROPERTY_DEFINITION;
import static org.activiti.explorer.ui.process.ProcessInstanceItem.PROPERTY_SUPER_ID;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * @author Joram Barrez
 */
class ProcessInstanceItem extends PropertysetItem implements Comparable<ProcessInstanceItem> {
	static final String PROPERTY_BUSINESS_KEY = "businessKey";
	static final String PROPERTY_DEFINITION = "definition";
	static final String PROPERTY_SUPER_ID = "superId";
	static final String PROPERTY_NAME = "name";
	static final String PROPERTY_ID = "id";

  private static final long serialVersionUID = 1L;
  
  public ProcessInstanceItem() {
    
  }
  
  public ProcessInstanceItem(ProcessInstance processInstance) {
    addItemProperty("id", new ObjectProperty<String>(processInstance.getId(), String.class));
    addItemProperty("businessKey", new ObjectProperty<String>(processInstance.getBusinessKey(), String.class));
  }
  
  public ProcessInstanceItem(HistoricProcessInstance processInstance,ProcessDefinition processDefinition){
	    addItemProperty(PROPERTY_ID, new ObjectProperty<String>(processInstance.getId(), String.class));
	    String itemName = getProcessDisplayName(processDefinition) + " (" + processInstance.getId() + ")";
	    addItemProperty(PROPERTY_NAME, new ObjectProperty<String>(itemName, String.class));
	    addItemProperty(PROPERTY_SUPER_ID, new ObjectProperty<String>(processInstance.getSuperProcessInstanceId(), String.class));
	    addItemProperty(PROPERTY_DEFINITION, new ObjectProperty<String>(getProcessDisplayName(processDefinition), String.class));
	    addItemProperty(PROPERTY_BUSINESS_KEY, new ObjectProperty<String>(processInstance.getBusinessKey(), String.class));
  }
  
  @Override
public String toString() {
	return getItemProperty(PROPERTY_NAME).getValue().toString();
}

  public int compareTo(ProcessInstanceItem other) {
    // process instances are ordered by id
    String id = (String) getItemProperty("id").getValue();
    String otherId = (String) other.getItemProperty("id").getValue();
    return id.compareTo(otherId);
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
	    if(processDefinition.getName() != null) {
	      return processDefinition.getName();
	    } else {
	      return processDefinition.getKey();
	    }
	  }
}
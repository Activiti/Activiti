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

import org.activiti.engine.runtime.ProcessInstance;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * @author Joram Barrez
 */
class ProcessInstanceItem extends PropertysetItem implements Comparable<ProcessInstanceItem> {

  private static final long serialVersionUID = 1L;
  
  public ProcessInstanceItem() {
    
  }
  
  public ProcessInstanceItem(ProcessInstance processInstance) {
    addItemProperty("id", new ObjectProperty<String>(processInstance.getId(), String.class));
    addItemProperty("businessKey", new ObjectProperty<String>(processInstance.getBusinessKey(), String.class));
  }

  public int compareTo(ProcessInstanceItem other) {
    // process instances are ordered by id
    String id = (String) getItemProperty("id").getValue();
    String otherId = (String) other.getItemProperty("id").getValue();
    return id.compareTo(otherId);
  }
}
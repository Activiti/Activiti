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

import org.activiti.engine.repository.ProcessDefinition;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * @author Joram Barrez
 */
public  class ReportListItem extends PropertysetItem implements Comparable<ReportListItem> {
  
  private static final long serialVersionUID = 1L;
  
  public ReportListItem(ProcessDefinition processDefinition) {
    addItemProperty("id", new ObjectProperty<String>(processDefinition.getId(), String.class));
    addItemProperty("key", new ObjectProperty<String>(processDefinition.getKey(), String.class));
    addItemProperty("name", new ObjectProperty<String>(processDefinition.getName() ,String.class));
    addItemProperty("version", new ObjectProperty<Integer>(processDefinition.getVersion() ,Integer.class));
  }
  
  public int compareTo(ReportListItem other) {
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
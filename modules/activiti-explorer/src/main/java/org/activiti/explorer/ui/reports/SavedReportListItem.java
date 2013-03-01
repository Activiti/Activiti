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

import java.text.DateFormat;
import java.util.Date;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * @author Frederik Heremans
 */
public  class SavedReportListItem extends PropertysetItem implements Comparable<SavedReportListItem> {
  
  private static final long serialVersionUID = 1L;
  
  public SavedReportListItem(HistoricProcessInstance historicProcessInstance) {
    addItemProperty("id", new ObjectProperty<String>(historicProcessInstance.getId(), String.class));
    addItemProperty("name", getNameProperty(historicProcessInstance));
    
    if(historicProcessInstance.getEndTime() == null) {
      throw new ActivitiIllegalArgumentException("The given process-instance is not ended yet");
    }
    addItemProperty("createTime", new ObjectProperty<Date>(historicProcessInstance.getEndTime(), Date.class));
  }
  
  public int compareTo(SavedReportListItem other) {
     Date createTime = (Date) getItemProperty("createTime").getValue(); 
     Date otherCreateTime = (Date) other.getItemProperty("createTime").getValue();
     
     return createTime.compareTo(otherCreateTime);
  }
  
  protected Property getNameProperty(HistoricProcessInstance historicProcessInstance) {
    return new ObjectProperty<String>(getReportDisplayName(historicProcessInstance), String.class);
  }
  
  public static String getReportDisplayName(HistoricProcessInstance historicProcessInstance) {
    if(historicProcessInstance.getBusinessKey() != null && !historicProcessInstance.getBusinessKey().isEmpty()) {
      if(Authentication.getAuthenticatedUserId() != null) {
        return historicProcessInstance.getBusinessKey().replaceFirst(Authentication.getAuthenticatedUserId() + "\\_", "");
      } else {
        return historicProcessInstance.getBusinessKey();
      }
    } else {
      return DateFormat.getDateTimeInstance().format(historicProcessInstance.getEndTime());
    }
  }
}
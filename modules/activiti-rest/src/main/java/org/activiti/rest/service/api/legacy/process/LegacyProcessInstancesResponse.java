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

package org.activiti.rest.service.api.legacy.process;

import java.io.Serializable;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.rest.common.api.RequestUtil;

/**
 * @author Tijs Rademakers
 */
public class LegacyProcessInstancesResponse implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  String id;
  String businessKey;
  String processDefinitionId;
  String startTime;
  String startUserId;
  
  public LegacyProcessInstancesResponse(HistoricProcessInstance processInstance) {
    this.setId(processInstance.getId());
    this.setBusinessKey(processInstance.getBusinessKey());
    this.setStartTime(RequestUtil.dateToString(processInstance.getStartTime()));
    this.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    this.setStartUserId(processInstance.getStartUserId());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }
}

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

package org.activiti.rest.api.history;

import java.util.Date;
import java.util.List;

import org.activiti.rest.api.engine.variable.QueryVariable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/**
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceQueryRequest {

  private String processInstanceId;
  private String processBusinessKey;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String superProcessInstanceId;
  private Boolean excludeSubprocesses;
  private Boolean finished;
  private String involvedUser;
  private Date finishedAfter;
  private Date finishedBefore;
  private Date startedAfter;
  private Date startedBefore;
  private String startedBy;
  private Boolean includeProcessVariables;
  private List<QueryVariable> variables;
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public String getProcessBusinessKey() {
    return processBusinessKey;
  }
  
  public void setProcessBusinessKey(String processBusinessKey) {
    this.processBusinessKey = processBusinessKey;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public Boolean getExcludeSubprocesses() {
    return excludeSubprocesses;
  }

  public void setExcludeSubprocesses(Boolean excludeSubprocesses) {
    this.excludeSubprocesses = excludeSubprocesses;
  }

  public Boolean getFinished() {
    return finished;
  }

  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  public String getInvolvedUser() {
    return involvedUser;
  }
  
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }
  
  public Date getFinishedAfter() {
    return finishedAfter;
  }

  public void setFinishedAfter(Date finishedAfter) {
    this.finishedAfter = finishedAfter;
  }

  public Date getFinishedBefore() {
    return finishedBefore;
  }

  public void setFinishedBefore(Date finishedBefore) {
    this.finishedBefore = finishedBefore;
  }

  public Date getStartedAfter() {
    return startedAfter;
  }

  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  public String getStartedBy() {
    return startedBy;
  }

  public void setStartedBy(String startedBy) {
    this.startedBy = startedBy;
  }

  public Boolean getIncludeProcessVariables() {
    return includeProcessVariables;
  }

  public void setIncludeProcessVariables(Boolean includeProcessVariables) {
    this.includeProcessVariables = includeProcessVariables;
  }

  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)  
  public List<QueryVariable> getVariables() {
    return variables;
  }
  
  public void setVariables(List<QueryVariable> variables) {
    this.variables = variables;
  }
}

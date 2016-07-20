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
package org.activiti.form.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Tijs Rademakers
 */
@JsonInclude(Include.NON_NULL)
public class CompletedFormDefinition extends FormDefinition {

  private static final long serialVersionUID = 1L;
  
  protected String submittedFormId;
  protected String submittedBy;
  protected Date submittedDate;
  protected String selectedOutcome;
  protected String taskId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String tenantId;
  
  public CompletedFormDefinition(FormDefinition formDefinition) {
    this.description = formDefinition.getDescription();
    this.fields = formDefinition.getFields();
    this.id = formDefinition.getId();
    this.key = formDefinition.getKey();
    this.name = formDefinition.getName();
    this.outcomes = formDefinition.getOutcomes();
    this.outcomeVariableName = formDefinition.getOutcomeVariableName();
    this.version = formDefinition.getVersion();
  }
  
  
  public String getSubmittedFormId() {
    return submittedFormId;
  }

  public void setSubmittedFormId(String submittedFormId) {
    this.submittedFormId = submittedFormId;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }
  
  public void setSubmittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
  }
  
  public Date getSubmittedDate() {
    return submittedDate;
  }
  
  public void setSubmittedDate(Date submittedDate) {
    this.submittedDate = submittedDate;
  }
  
  public String getSelectedOutcome() {
    return selectedOutcome;
  }
  
  public void setSelectedOutcome(String selectedOutcome) {
    this.selectedOutcome = selectedOutcome;
  }
  
  public String getTaskId() {
    return taskId;
  }
  
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public String getTenantId() {
    return tenantId;
  }
  
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

}
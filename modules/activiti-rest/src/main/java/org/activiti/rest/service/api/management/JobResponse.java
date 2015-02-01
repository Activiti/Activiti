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

package org.activiti.rest.service.api.management;

import java.util.Date;

import org.activiti.rest.common.util.DateToStringSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * @author Frederik Heremans
 */
public class JobResponse {

  protected String id;
  protected String url;
  protected String processInstanceId;
  protected String processInstanceUrl;
  protected String processDefinitionId;
  protected String processDefinitionUrl;
  protected String executionId;
  protected String executionUrl;
  protected Integer retries;
  protected String exceptionMessage;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date dueDate;
  protected String tenantId;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public String getProcessInstanceUrl() {
    return processInstanceUrl;
  }
  
  public void setProcessInstanceUrl(String processInstanceUrl) {
    this.processInstanceUrl = processInstanceUrl;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public String getProcessDefinitionUrl() {
    return processDefinitionUrl;
  }
  
  public void setProcessDefinitionUrl(String processDefinitionUrl) {
    this.processDefinitionUrl = processDefinitionUrl;
  }
  
  public String getExecutionId() {
    return executionId;
  }
  
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  public String getExecutionUrl() {
    return executionUrl;
  }
  
  public void setExecutionUrl(String executionUrl) {
    this.executionUrl = executionUrl;
  }
  
  public Integer getRetries() {
    return retries;
  }
  
  public void setRetries(Integer retries) {
    this.retries = retries;
  }
  
  public String getExceptionMessage() {
    return exceptionMessage;
  }
  
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }
  
  public Date getDueDate() {
    return dueDate;
  }
  
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  
  public void setTenantId(String tenantId) {
	  this.tenantId = tenantId;
  }
  
  public String getTenantId() {
	  return tenantId;
  }
}

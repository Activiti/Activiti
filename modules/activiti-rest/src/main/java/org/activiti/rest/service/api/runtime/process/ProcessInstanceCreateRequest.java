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

package org.activiti.rest.service.api.runtime.process;

import java.util.List;

import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/**
 * Modified to add a "returnVariables" flag, which determines whether the variables
 *   that exist within the process instance when the first wait state is encountered
 *   (or when the process instance completes) should be returned or not.
 * 
 * @author Frederik Heremans
 * @author Ryan Johnston (@rjfsu)
 */
public class ProcessInstanceCreateRequest {

  private String processDefinitionId;
  private String processDefinitionKey;
  private String message;
  private String businessKey;
  private List<RestVariable> variables;
  private String tenantId;
  
  //Added by Ryan Johnston
  private boolean returnVariables;
  
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
  
  public String getBusinessKey() {
    return businessKey;
  }
  
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  
  public void setTenantId(String tenantId) {
	  this.tenantId = tenantId;
  }
  
  public String getTenantId() {
	  return tenantId;
  }
  
  @JsonTypeInfo(use=Id.CLASS, defaultImpl=RestVariable.class)  
  public List<RestVariable> getVariables() {
    return variables;
  }
  
  public void setVariables(List<RestVariable> variables) {
    this.variables = variables;
  }
  
  @JsonIgnore
  public boolean isCustomTenantSet() {
  	return tenantId != null && !StringUtils.isEmpty(tenantId);
  }
  
  //Added by Ryan Johnston
  public boolean getReturnVariables() {
	  return returnVariables;
  }
  
  //Added by Ryan Johnston
  public void setReturnVariables(boolean returnVariables) {
	  this.returnVariables = returnVariables;
  }
}

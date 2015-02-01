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

public class SignalEventReceivedRequest {

  private String signalName;
  private List<RestVariable> variables;
  private String tenantId;
  private boolean async = false;
  
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
  
  public String getSignalName() {
	  return signalName;
  }
  
  public void setSignalName(String signalName) {
	  this.signalName = signalName;
  }
  
  public void setAsync(boolean async) {
	  this.async = async;
  }
  
  public boolean isAsync() {
	  return async;
  }
}

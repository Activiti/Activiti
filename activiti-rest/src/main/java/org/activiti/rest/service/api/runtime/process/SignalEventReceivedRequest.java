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

import io.swagger.annotations.ApiModelProperty;

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
  
  @ApiModelProperty(value ="ID of the tenant that the signal event should be processed in")
  public String getTenantId() {
    return tenantId;
  }

  @JsonTypeInfo(use = Id.CLASS, defaultImpl = RestVariable.class)
  @ApiModelProperty(value ="Array of variables (in the general variables format) to use as payload to pass along with the signal. Cannot be used in case async is set to true, this will result in an error.")
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
  
  @ApiModelProperty(value ="Name of the signal")
  public String getSignalName() {
    return signalName;
  }

  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }
  @ApiModelProperty(value = "If true, handling of the signal will happen asynchronously. Return code will be 202 - Accepted to indicate the request is accepted but not yet executed. If false,\n"
          + "                    handling the signal will be done immediately and result (200 - OK) will only return after this completed successfully. Defaults to false if omitted.")
  public boolean isAsync() {
    return async;
  }
}

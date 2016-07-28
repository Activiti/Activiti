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
package org.activiti.rest.dmn.service.api.decision;

import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class ExecuteDecisionRequest {

  protected String decisionKey;
  protected String tenantId;
  protected Map<String, Object> inputVariables;

  public String getDecisionKey() {
    return decisionKey;
  }

  public void setDecisionKey(String decisionKey) {
    this.decisionKey = decisionKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Map<String, Object> getInputVariables() {
    return inputVariables;
  }

  public void setInputVariables(Map<String, Object> variables) {
    this.inputVariables = variables;
  }
}

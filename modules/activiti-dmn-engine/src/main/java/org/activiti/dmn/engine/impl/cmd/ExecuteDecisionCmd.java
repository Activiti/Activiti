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
package org.activiti.dmn.engine.impl.cmd;

import java.io.Serializable;
import java.util.Map;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.interceptor.Command;
import org.activiti.dmn.engine.impl.interceptor.CommandContext;
import org.activiti.dmn.engine.impl.persistence.deploy.DecisionTableCacheEntry;
import org.activiti.dmn.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.dmn.model.Decision;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class ExecuteDecisionCmd implements Command<RuleEngineExecutionResult>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String decisionKey;
  protected String parentDeploymentId;
  protected Map<String, Object> variables;
  protected String tenantId;

  public ExecuteDecisionCmd(String decisionKey, Map<String, Object> variables) {
    this.decisionKey = decisionKey;
    this.variables = variables;
  }
  
  public ExecuteDecisionCmd(String decisionKey, String parentDeploymentId, Map<String, Object> variables) {
    this(decisionKey, variables);
    this.parentDeploymentId = parentDeploymentId;
  }

  public ExecuteDecisionCmd(String decisionKey, String parentDeploymentId, Map<String, Object> variables, String tenantId) {
    this(decisionKey, parentDeploymentId, variables);
    this.tenantId = tenantId;
  }

  public RuleEngineExecutionResult execute(CommandContext commandContext) {
    if (decisionKey == null) {
      throw new ActivitiDmnIllegalArgumentException("decisionKey is null");
    }

    DmnEngineConfiguration dmnEngineConfiguration = commandContext.getDmnEngineConfiguration();
    DeploymentManager deploymentManager = dmnEngineConfiguration.getDeploymentManager();
    DmnDecisionTable decisionTable = null;

    if (StringUtils.isNotEmpty(decisionKey) && StringUtils.isNotEmpty(parentDeploymentId) && StringUtils.isNotEmpty(tenantId)) {
      decisionTable = deploymentManager.findDeployedLatestDecisionByKeyParentDeploymentIdAndTenantId(decisionKey, parentDeploymentId, tenantId);
      if (decisionTable == null) {
        throw new ActivitiDmnObjectNotFoundException("No decision found for key: " + decisionKey + 
            ", parent deployment id " + parentDeploymentId + " and tenant id: " + tenantId);
      }
      
    } else if (StringUtils.isNotEmpty(decisionKey) && StringUtils.isNotEmpty(parentDeploymentId)) {
      decisionTable = deploymentManager.findDeployedLatestDecisionByKeyAndParentDeploymentId(decisionKey, parentDeploymentId);
      if (decisionTable == null) {
        throw new ActivitiDmnObjectNotFoundException("No decision found for key: " + decisionKey + 
            " and parent deployment id " + parentDeploymentId);
      }
      
    } else if (StringUtils.isNotEmpty(decisionKey) && StringUtils.isNotEmpty(tenantId)) {
      decisionTable = deploymentManager.findDeployedLatestDecisionByKeyAndTenantId(decisionKey, tenantId);
      if (decisionTable == null) {
        throw new ActivitiDmnObjectNotFoundException("No decision found for key: " + decisionKey + 
            " and tenant id " + tenantId);
      }
      
    } else if (StringUtils.isNotEmpty(decisionKey)) {
      decisionTable = deploymentManager.findDeployedLatestDecisionByKey(decisionKey);
      if (decisionTable == null) {
        throw new ActivitiDmnObjectNotFoundException("No decision found for key: " + decisionKey);
      }
      
    } else {
      throw new IllegalArgumentException("decisionKey is null");
    }

    DecisionTableCacheEntry decisionTableCacheEntry = deploymentManager.resolveDecisionTable(decisionTable);
    Decision decision = decisionTableCacheEntry.getDecision();

    RuleEngineExecutionResult executionResult = dmnEngineConfiguration.getRuleEngineExecutor().execute(decision, variables, 
        dmnEngineConfiguration.getCustomExpressionFunctions(), dmnEngineConfiguration.getCustomPropertyHandlers());

    if (executionResult != null && executionResult.getAuditTrail() != null) {
      executionResult.getAuditTrail().setDmnDeploymentId(decisionTable.getDeploymentId());
    }

    return executionResult;

  }

}

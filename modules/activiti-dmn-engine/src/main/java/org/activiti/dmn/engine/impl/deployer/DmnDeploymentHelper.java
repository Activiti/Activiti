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
package org.activiti.dmn.engine.impl.deployer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.context.Context;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * Methods for working with deployments.  Much of the actual work of {@link DmnDeployer} is
 * done by orchestrating the different pieces of work this class does; by having them here,
 * we allow other deployers to make use of them.   
 */
public class DmnDeploymentHelper  {
  
  /**
   * Verifies that no two decision tables share the same key, to prevent database unique index violation.
   * 
   * @throws ActivitiException if any two decision tables have the same key
   */
  public void verifyDecisionTablesDoNotShareKeys(Collection<DecisionTableEntity> decisionTables) {
    Set<String> keySet = new LinkedHashSet<String>();
    for (DecisionTableEntity decisionTable : decisionTables) {
      if (keySet.contains(decisionTable.getKey())) {
        throw new ActivitiDmnException(
            "The deployment contains decision tables with the same key (decision id attribute), this is not allowed");
      }
      keySet.add(decisionTable.getKey());
    }
  }

  /**
   * Updates all the decision table entities to match the deployment's values for tenant,
   * engine version, and deployment id.
   */
  public void copyDeploymentValuesToDecisionTables(DmnDeploymentEntity deployment, List<DecisionTableEntity> decisionTables) {
    String tenantId = deployment.getTenantId();
    String deploymentId = deployment.getId();

    for (DecisionTableEntity decisionTable : decisionTables) {
      
      // decision table inherits the tenant id
      if (tenantId != null) {
        decisionTable.setTenantId(tenantId); 
      }

      decisionTable.setDeploymentId(deploymentId);
    }
  }

  /**
   * Updates all the decision table entities to have the correct resource names.
   */
  public void setResourceNamesOnDecisionTables(ParsedDeployment parsedDeployment) {
    for (DecisionTableEntity decisionTable : parsedDeployment.getAllDecisionTables()) {
      String resourceName = parsedDeployment.getResourceForDecisionTable(decisionTable).getName();
      decisionTable.setResourceName(resourceName);
    }
  }

  /**
   * Gets the most recent persisted decision table that matches this one for tenant and key.
   * If none is found, returns null.  This method assumes that the tenant and key are properly
   * set on the decision table entity.
   */
  public DecisionTableEntity getMostRecentVersionOfDecisionTable(DecisionTableEntity decisionTable) {
    String key = decisionTable.getKey();
    String tenantId = decisionTable.getTenantId();
    DecisionTableEntityManager decisionTableEntityManager = Context.getCommandContext().getDmnEngineConfiguration().getDecisionTableEntityManager();

    DecisionTableEntity existingDefinition = null;

    if (tenantId != null && !tenantId.equals(DmnEngineConfiguration.NO_TENANT_ID)) {
      existingDefinition = decisionTableEntityManager.findLatestDecisionTableByKeyAndTenantId(key, tenantId);
    } else {
      existingDefinition = decisionTableEntityManager.findLatestDecisionTableByKey(key);
    }

    return existingDefinition;
  }

  /**
   * Gets the persisted version of the already-deployed process definition.  Note that this is
   * different from {@link #getMostRecentVersionOfProcessDefinition} as it looks specifically for
   * a process definition that is already persisted and attached to a particular deployment,
   * rather than the latest version across all deployments.
   */
  public DecisionTableEntity getPersistedInstanceOfDecisionTable(DecisionTableEntity decisionTable) {
    String deploymentId = decisionTable.getDeploymentId();
    if (StringUtils.isEmpty(decisionTable.getDeploymentId())) {
      throw new ActivitiDmnIllegalArgumentException("Provided process definition must have a deployment id.");
    }

    DecisionTableEntityManager decisionTableEntityManager = Context.getCommandContext().getDmnEngineConfiguration().getDecisionTableEntityManager();
    DecisionTableEntity persistedDecisionTable = null;
    if (decisionTable.getTenantId() == null || DmnEngineConfiguration.NO_TENANT_ID.equals(decisionTable.getTenantId())) {
      persistedDecisionTable = decisionTableEntityManager.findDecisionTableByDeploymentAndKey(deploymentId, decisionTable.getKey());
    } else {
      persistedDecisionTable = decisionTableEntityManager.findDecisionTableByDeploymentAndKeyAndTenantId(deploymentId, decisionTable.getKey(), decisionTable.getTenantId());
    }

    return persistedDecisionTable;
  }
}


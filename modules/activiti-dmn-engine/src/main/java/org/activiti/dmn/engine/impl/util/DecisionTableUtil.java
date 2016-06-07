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
package org.activiti.dmn.engine.impl.util;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.impl.context.Context;
import org.activiti.dmn.engine.impl.persistence.deploy.DecisionTableCacheEntry;
import org.activiti.dmn.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManager;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DmnDefinition;

/**
 * A utility class that hides the complexity of {@link DecisionTableEntity} and {@link Decision} lookup. 
 * Use this class rather than accessing the decision table cache or {@link DeploymentManager} directly.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DecisionTableUtil {

  public static DecisionTableEntity getDecisionTableEntity(String decisionTableId) {
    return getDecisionTableEntity(decisionTableId, false);
  }

  public static DecisionTableEntity getDecisionTableEntity(String decisionTableId, boolean checkCacheOnly) {
    if (checkCacheOnly) {
      DecisionTableCacheEntry cacheEntry = Context.getDmnEngineConfiguration().getDecisionCache().get(decisionTableId);
      if (cacheEntry != null) {
        return cacheEntry.getDecisionTableEntity();
      }
      return null;
    } else {
      // This will check the cache in the findDeployedProcessDefinitionById method
      return Context.getDmnEngineConfiguration().getDeploymentManager().findDeployedDecisionById(decisionTableId);
    }
  }

  public static Decision getDecision(String decisionTableId) {
    DeploymentManager deploymentManager = Context.getDmnEngineConfiguration().getDeploymentManager();
      
    // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
    DecisionTableEntity decisionTableEntity = deploymentManager.findDeployedDecisionById(decisionTableId);
    return deploymentManager.resolveDecisionTable(decisionTableEntity).getDecision();
  }

  public static DmnDefinition getDmnDefinition(String decisionTableId) {
    DeploymentManager deploymentManager = Context.getDmnEngineConfiguration().getDeploymentManager();
      
    // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
    DecisionTableEntity decisionTableEntity = deploymentManager.findDeployedDecisionById(decisionTableId);
    return deploymentManager.resolveDecisionTable(decisionTableEntity).getDmnDefinition();
  }
  
  public static DmnDefinition getDmnDefinitionFromCache(String decisionTableId) {
    DecisionTableCacheEntry cacheEntry = Context.getDmnEngineConfiguration().getDecisionCache().get(decisionTableId);
    if (cacheEntry != null) {
      return cacheEntry.getDmnDefinition();
    }
    return null;
  }
  
  public static DecisionTableEntity getDecisionTableFromDatabase(String decisionTableId) {
    DecisionTableEntityManager decisionTableEntityManager = Context.getDmnEngineConfiguration().getDecisionTableEntityManager();
    DecisionTableEntity decisionTable = decisionTableEntityManager.findById(decisionTableId);
    if (decisionTable == null) {
      throw new ActivitiDmnException("No decision table found with id " + decisionTableId);
    }
    
    return decisionTable;
  }
}

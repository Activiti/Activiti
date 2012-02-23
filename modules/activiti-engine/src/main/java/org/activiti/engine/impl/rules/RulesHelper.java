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

package org.activiti.engine.impl.rules;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.drools.KnowledgeBase;


/**
 * @author Tom Baeyens
 */
public class RulesHelper {

  public static KnowledgeBase findKnowledgeBaseByDeploymentId(String deploymentId) {
    Map<String, Object> knowledgeBaseCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .getKnowledgeBaseCache();
  
    KnowledgeBase knowledgeBase = (KnowledgeBase) knowledgeBaseCache.get(deploymentId);
    if (knowledgeBase==null) {
      DeploymentEntity deployment = Context
        .getCommandContext()
        .getDeploymentManager()
        .findDeploymentById(deploymentId);
      if (deployment==null) {
        throw new ActivitiException("no deployment with id "+deploymentId);
      }
      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .deploy(deployment);
      knowledgeBase = (KnowledgeBase) knowledgeBaseCache.get(deploymentId);
      if (knowledgeBase==null) {
        throw new ActivitiException("deployment "+deploymentId+" doesn't contain any rules");
      }
    }
    return knowledgeBase;
  }
}

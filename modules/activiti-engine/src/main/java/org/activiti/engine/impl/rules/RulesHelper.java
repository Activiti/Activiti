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

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.db.DbRepositorySessionFactory;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.repository.Deployment;
import org.drools.KnowledgeBase;


/**
 * @author Tom Baeyens
 */
public class RulesHelper {

  public static KnowledgeBase findKnowledgeBaseByDeploymentId(String deploymentId) {
    CommandContext commandContext = CommandContext.getCurrent();
    DbRepositorySessionFactory repositorySessionFactory = (DbRepositorySessionFactory) commandContext
      .getProcessEngineConfiguration()
      .getSessionFactories()
      .get(RepositorySession.class);
  
    Map<String, Object> knowledgeBaseCache = repositorySessionFactory.getKnowledgeBaseCache();
    KnowledgeBase knowledgeBase = (KnowledgeBase) knowledgeBaseCache.get(deploymentId);
    if (knowledgeBase==null) {
      RepositorySession repositorySession = commandContext.getRepositorySession();
      DeploymentEntity deployment = repositorySession.findDeploymentById(deploymentId);
      if (deployment==null) {
        throw new ActivitiException("no deployment with id "+deploymentId);
      }
      repositorySession.deploy(deployment);
      knowledgeBase = (KnowledgeBase) knowledgeBaseCache.get(deploymentId);
      if (knowledgeBase==null) {
        throw new ActivitiException("deployment "+deploymentId+" doesn't contain any rules");
      }
    }
    return knowledgeBase;
  }

  public static KnowledgeBase findLatestKnowledgeBaseByDeploymentName(String deploymentName) {
    DeploymentQueryImpl deploymentQuery = new DeploymentQueryImpl(CommandContext.getCurrent());
    deploymentQuery
      .deploymentName(deploymentName)
      .orderByDeploymenTime().asc();
    List<Deployment> deployments = deploymentQuery.listPage(0, 1);
    if (deployments.isEmpty()) {
      throw new ActivitiException("no deployments with name "+deploymentName);
    }
    String deploymentId = deployments.get(0).getId();
    return findKnowledgeBaseByDeploymentId(deploymentId);
  }
}

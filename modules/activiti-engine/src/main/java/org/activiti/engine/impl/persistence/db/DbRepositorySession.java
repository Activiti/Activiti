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

package org.activiti.engine.impl.persistence.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 */
public class DbRepositorySession implements Session, RepositorySession {

  protected DbRepositorySessionFactory dbRepositorySessionFactory;
  protected DbSqlSession dbSqlSession;
  
  public DbRepositorySession(DbRepositorySessionFactory dbRepositorySessionFactory) {
    this.dbRepositorySessionFactory = dbRepositorySessionFactory;
    this.dbSqlSession = CommandContext
      .getCurrent()
      .getSession(DbSqlSession.class);
  }

  public void close() {
  }

  public void flush() {
  }

  public void deployNew(DeploymentEntity deployment) {
    dbSqlSession.insert(deployment);
    for (ResourceEntity resource: deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      dbSqlSession.insert(resource);
    }
    for (Deployer deployer: dbRepositorySessionFactory.getDeployers()) {
      List<ProcessDefinitionEntity> processDefinitions = deployer.deploy(deployment);
      for (ProcessDefinitionEntity processDefinition : processDefinitions) {
        processDefinition.setDeploymentId(deployment.getId());
        dbSqlSession.insert(processDefinition);
        addToProcessDefinitionCache(processDefinition);
      }
    }
  }

  public void deployExisting(DeploymentEntity deployment) {
    for (Deployer deployer: dbRepositorySessionFactory.getDeployers()) {
      List<ProcessDefinitionEntity> processDefinitions = deployer.deploy(deployment);
      for (ProcessDefinitionEntity processDefinition : processDefinitions) {
        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        ProcessDefinitionEntity persistedProcessDefinition = findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        addToProcessDefinitionCache(processDefinition);
      }
    }
  }

  protected void addToProcessDefinitionCache(ProcessDefinitionEntity processDefinition) {
    Map<String, ProcessDefinitionEntity> processDefinitionCache = dbRepositorySessionFactory.getProcessDefinitionCache();
    String processDefinitionId = processDefinition.getId();
    processDefinitionCache.put(processDefinitionId, processDefinition);
  }

  public void deleteDeployment(String deploymentId) {
    dbSqlSession.delete("deleteProcessDefinitionsByDeploymentId", deploymentId);
    dbSqlSession.delete("deleteResourcesByDeploymentId", deploymentId);
    dbSqlSession.delete("deleteDeploymentById", deploymentId);
  }
  
  public DeploymentEntity findDeploymentById(String deploymentId) {
    return (DeploymentEntity) dbSqlSession.selectOne("selectDeploymentById", deploymentId);
  }

  public ResourceEntity findResourceByDeploymentIdAndResourceName(String deploymentId, String resourceName) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceName", resourceName);
    return (ResourceEntity) dbSqlSession.selectOne("selectResourceByDeploymentIdAndResourceName", params);
  }

  @SuppressWarnings("unchecked")
  public List<ResourceEntity> findResourcesByDeploymentId(String deploymentId) {
    return dbSqlSession.selectList("selectResourcesByDeploymentId", deploymentId);
  }
  
  @SuppressWarnings("unchecked")
  public List<String> findDeploymentResourceNames(String deploymentId) {
    return dbSqlSession.selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<DeploymentEntity> findDeployments() {
    return (List<DeploymentEntity>) dbSqlSession.selectList("selectDeployments");
  };

  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    return (DeploymentEntity) dbSqlSession.selectOne("selectLatestDeploymentByName", deploymentName);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionImpl> findProcessDefinitions() {
    return dbSqlSession.selectList("selectProcessDefinitions");
  }

  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) dbSqlSession.selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionEntity> findProcessDefinitionsByDeploymentId(String deploymentId) {
    return dbSqlSession.selectList("selectProcessDefinitionsByDeploymentId", deploymentId);
  }

  public ProcessDefinitionEntity findProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionEntity) dbSqlSession.selectOne("selectProcessDefinitionById", processDefinitionId);
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) dbSqlSession.selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) dbSqlSession.selectOne("selectProcessDefinitionById", processDefinitionId);
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  protected ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();
    processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get(processDefinitionId);
    if (processDefinition==null) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      deployExisting(deployment);
      processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get(processDefinitionId);
    }
    return processDefinition;
  }
}

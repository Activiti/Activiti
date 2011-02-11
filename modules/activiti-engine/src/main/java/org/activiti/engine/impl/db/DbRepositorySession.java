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

package org.activiti.engine.impl.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.repository.Deployer;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;


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

  public void deploy(DeploymentEntity deployment) {
    if (deployment.isNew()) {
      dbSqlSession.insert(deployment);
      for (ResourceEntity resource : deployment.getResources().values()) {
        resource.setDeploymentId(deployment.getId());
        dbSqlSession.insert(resource);
      }
    }
    for (Deployer deployer: dbRepositorySessionFactory.getDeployers()) {
      deployer.deploy(deployment);
    }
  }

  public void addToProcessDefinitionCache(ProcessDefinitionEntity processDefinition) {
    Map<String, ProcessDefinitionEntity> processDefinitionCache = dbRepositorySessionFactory.getProcessDefinitionCache();
    String processDefinitionId = processDefinition.getId();
    processDefinitionCache.put(processDefinitionId, processDefinition);
  }

  public void deleteDeployment(String deploymentId, boolean cascade) {
    if (cascade) {
      CommandContext commandContext = CommandContext.getCurrent();
      List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(commandContext)
        .deploymentId(deploymentId)
        .list();

      int historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
      boolean isHistoryEnabled = historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY;
      
      for (ProcessDefinition processDefinition: processDefinitions) {
        if (isHistoryEnabled) {
          deleteHistoricProcessInstances(commandContext, processDefinition);
        }
        deleteProcessInstances(commandContext, processDefinition);
      }
    }
    dbSqlSession.delete("deleteProcessDefinitionsByDeploymentId", deploymentId);
    dbSqlSession.delete("deleteResourcesByDeploymentId", deploymentId);
    dbSqlSession.delete("deleteDeployment", deploymentId);
  }

  private void deleteProcessInstances(CommandContext commandContext, ProcessDefinition processDefinition) {
    List<ProcessInstance> processInstances = new ProcessInstanceQueryImpl(commandContext)
        .processDefinitionId(processDefinition.getId())
        .list();
      
      for (ProcessInstance processInstance: processInstances) {
        commandContext
          .getRuntimeSession()
          .deleteProcessInstance(processInstance.getId(), "deleted deployment");
      }
  }

  private void deleteHistoricProcessInstances(CommandContext commandContext, ProcessDefinition processDefinition) {
    List<HistoricProcessInstance> historicProcessInstances = new HistoricProcessInstanceQueryImpl(commandContext)
      .processDefinitionId(processDefinition.getId())
      .list();
    
    for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
      commandContext
        .getHistorySession()
        .deleteHistoricProcessInstance(historicProcessInstance.getId());
    }
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
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return dbSqlSession.getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<DeploymentEntity> findDeployments() {
    return (List<DeploymentEntity>) dbSqlSession.selectList("selectDeployments");
  };

  @SuppressWarnings("rawtypes")
  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List list = dbSqlSession.selectList("selectDeploymentsByName", deploymentName, new Page(0, 1));
    if (list!=null && !list.isEmpty()) {
      return (DeploymentEntity) list.get(0);
    }
    return null;
  }

  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) dbSqlSession.selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionEntity) dbSqlSession.selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) dbSqlSession.selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
    if (processDefinition==null) {
      throw new ActivitiException("no processes deployed with key '"+processDefinitionKey+"'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Invalid process definition id : null");
    }
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) dbSqlSession.selectOne("selectProcessDefinitionById", processDefinitionId);
    if(processDefinition == null) {
      throw new ActivitiException("no deployed process definition found with id '" + processDefinitionId + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  protected ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();
    processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get(processDefinitionId);
    if (processDefinition==null) {
      DeploymentEntity deployment = findDeploymentById(deploymentId);
      deployment.setNew(false);
      deploy(deployment);
      processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get(processDefinitionId);
    }
    return processDefinition;
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
    final String query = "selectProcessDefinitionsByQueryCriteria";
    return dbSqlSession.selectList(query, processDefinitionQuery, page);
  }

  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    return (Long) dbSqlSession.selectOne("selectProcessDefinitionCountByQueryCriteria", processDefinitionQuery);
  }

  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    return (Long) dbSqlSession.selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return dbSqlSession.selectList(query, deploymentQuery, page);
  }
  
  public void close() {
  }

  public void flush() {
  }
  
}

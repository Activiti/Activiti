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

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class ProcessDefinitionEntityManager extends AbstractManager {

  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
  }
  
  public ProcessDefinitionEntity findLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
  	Map<String, Object> params = new HashMap<String, Object>(2);
  	params.put("processDefinitionKey", processDefinitionKey);
  	params.put("tenantId", tenantId);
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectLatestProcessDefinitionByKeyAndTenantId", params);
  }

  public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
    getDbSqlSession().delete("deleteProcessDefinitionsByDeploymentId", deploymentId);
  }

  public ProcessDefinitionEntity findProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionById", processDefinitionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
//    List<ProcessDefinition> processDefinitions = 
    return getDbSqlSession().selectList("selectProcessDefinitionsByQueryCriteria", processDefinitionQuery, page);

    //skipped this after discussion within the team
//    // retrieve process definitions from cache (https://activiti.atlassian.net/browse/ACT-1020) to have all available information
//    ArrayList<ProcessDefinition> result = new ArrayList<ProcessDefinition>();
//    for (ProcessDefinition processDefinitionEntity : processDefinitions) {      
//      ProcessDefinitionEntity fullProcessDefinition = Context
//              .getProcessEngineConfiguration()
//              .getDeploymentCache().resolveProcessDefinition((ProcessDefinitionEntity)processDefinitionEntity);
//      result.add(fullProcessDefinition);
//    }
//    return result;
  }

  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessDefinitionCountByQueryCriteria", processDefinitionQuery);
  }
  
  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }
  
  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKeyAndTenantId(String deploymentId, String processDefinitionKey, String tenantId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("tenantId", tenantId);
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionByDeploymentAndKeyAndTenantId", parameters);
  }

  public ProcessDefinition findProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionQueryImpl processDefinitionQuery = new ProcessDefinitionQueryImpl()
      .processDefinitionKey(processDefinitionKey)
      .processDefinitionVersion(processDefinitionVersion);
    List<ProcessDefinition> results = findProcessDefinitionsByQueryCriteria(processDefinitionQuery, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiException("There are " + results.size() + " process definitions with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'.");
    }
    return null; 
  }
  
  public List<ProcessDefinition> findProcessDefinitionsStartableByUser(String user) {
    return   new ProcessDefinitionQueryImpl().startableByUser(user).list();
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectProcessDefinitionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findProcessDefinitionCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectProcessDefinitionCountByNativeQuery", parameterMap);
  }
  
  public void updateProcessDefinitionTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateProcessDefinitionTenantIdForDeploymentId", params);
  }
 
}

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

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Frederik Heremans
 */
public class HistoricIdentityLinkEntityManager extends AbstractManager {

  public void deleteHistoricIdentityLink(HistoricIdentityLinkEntity identityLink) {
    getDbSqlSession().delete(identityLink);
  }
  
  public void deleteHistoricIdentityLink(String id) {
    getDbSqlSession().delete("deleteHistoricIdentityLink", id);
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinksByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectHistoricIdentityLinksByTask", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectHistoricIdentityLinksByProcessInstance", processInstanceId);
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinksByProcessDefinitionId(String processDefinitionId) {
    return getDbSqlSession().selectList("selectHistoricIdentityLinksByProcessDefinition", processDefinitionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinks() {
    return getDbSqlSession().selectList("selectHistoricIdentityLinks");
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return getDbSqlSession().selectList("selectHistoricIdentityLinkByTaskUserGroupAndType", parameters);
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinkByProcessDefinitionUserAndGroup(String processDefinitionId, String userId, String groupId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    return getDbSqlSession().selectList("selectHistoricIdentityLinkByProcessDefinitionUserAndGroup", parameters);
  }

  public void deleteHistoricIdentityLinksByTaskId(String taskId) {
    List<HistoricIdentityLinkEntity> identityLinks = findHistoricIdentityLinksByTaskId(taskId);
    for (HistoricIdentityLinkEntity identityLink: identityLinks) {
      deleteHistoricIdentityLink(identityLink);
    }
  }

  public void deleteHistoricIdentityLinksByProcInstance(String processInstanceId) {
    
    // Identity links from db
    List<HistoricIdentityLinkEntity> identityLinks = findHistoricIdentityLinksByProcessInstanceId(processInstanceId);
    // Delete
    for (HistoricIdentityLinkEntity identityLink: identityLinks) {
      deleteHistoricIdentityLink(identityLink);
    }
    
    // Identity links from cache
    List<HistoricIdentityLinkEntity> identityLinksFromCache = Context.getCommandContext().getDbSqlSession().findInCache(HistoricIdentityLinkEntity.class);
    for (HistoricIdentityLinkEntity identityLinkEntity : identityLinksFromCache) {
      if (processInstanceId.equals(identityLinkEntity.getProcessInstanceId())) {
        deleteHistoricIdentityLink(identityLinkEntity);
      }
    }
  }
  
  public void deleteHistoricIdentityLinksByProcDef(String processDefId) {
    getDbSqlSession().delete("deleteHistoricIdentityLinkByProcDef", processDefId);
  }
}

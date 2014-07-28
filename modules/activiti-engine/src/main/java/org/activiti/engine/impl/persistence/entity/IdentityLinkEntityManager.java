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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 */
public class IdentityLinkEntityManager extends AbstractManager {

  public void deleteIdentityLink(IdentityLinkEntity identityLink, boolean cascadeHistory) {
    getDbSqlSession().delete(identityLink);
    if(cascadeHistory) {
      getHistoryManager().deleteHistoricIdentityLink(identityLink.getId());
    }
    
    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, identityLink));
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectIdentityLinksByTask", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectIdentityLinksByProcessInstance", processInstanceId);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByProcessDefinitionId(String processDefinitionId) {
    return getDbSqlSession().selectList("selectIdentityLinksByProcessDefinition", processDefinitionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinks() {
    return getDbSqlSession().selectList("selectIdentityLinks");
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return getDbSqlSession().selectList("selectIdentityLinkByTaskUserGroupAndType", parameters);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByProcessInstanceUserGroupAndType(String processInstanceId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return getDbSqlSession().selectList("selectIdentityLinkByProcessInstanceUserGroupAndType", parameters);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByProcessDefinitionUserAndGroup(String processDefinitionId, String userId, String groupId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    return getDbSqlSession().selectList("selectIdentityLinkByProcessDefinitionUserAndGroup", parameters);
  }

  public void deleteIdentityLinksByTaskId(String taskId) {
    List<IdentityLinkEntity> identityLinks = findIdentityLinksByTaskId(taskId);
    for (IdentityLinkEntity identityLink: identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
  }

  public void deleteIdentityLinksByProcInstance(String processInstanceId) {
    
    // Identity links from db
    List<IdentityLinkEntity> identityLinks = findIdentityLinksByProcessInstanceId(processInstanceId);
    // Delete
    for (IdentityLinkEntity identityLink: identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
    
    // Identity links from cache, if not already deleted
    List<IdentityLinkEntity> identityLinksFromCache = Context.getCommandContext().getDbSqlSession().findInCache(IdentityLinkEntity.class);
    boolean alreadyDeleted = false;
    for (IdentityLinkEntity identityLinkEntity : identityLinksFromCache) {
      if (processInstanceId.equals(identityLinkEntity.getProcessInstanceId())) {
      	alreadyDeleted = false;
      	for(IdentityLinkEntity deleted : identityLinks) {
      		if(deleted.getId() != null && deleted.getId().equals(identityLinkEntity.getId())) {
      			alreadyDeleted = true;
      			break;
      		}
      	}
      	
      	if(!alreadyDeleted) {
      		deleteIdentityLink(identityLinkEntity, false);
      	}
      }
    }
  }
  
  public void deleteIdentityLinksByProcDef(String processDefId) {
    getDbSqlSession().delete("deleteIdentityLinkByProcDef", processDefId);
  }

}

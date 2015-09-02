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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.task.IdentityLinkType;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class IdentityLinkEntityManagerImpl extends AbstractEntityManager<IdentityLinkEntity> implements IdentityLinkEntityManager {
  
  @Override
  public Class<IdentityLinkEntity> getManagedEntity() {
    return IdentityLinkEntity.class;
  }
  
  @Override
  public void insert(IdentityLinkEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);
    getHistoryManager().recordIdentityLinkCreated(entity);
  }

  @Override
  public void deleteIdentityLink(IdentityLinkEntity identityLink, boolean cascadeHistory) {
    getDbSqlSession().delete(identityLink);
    if (cascadeHistory) {
      getHistoryManager().deleteHistoricIdentityLink(identityLink.getId());
    }

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, identityLink));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectIdentityLinksByTask", taskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectIdentityLinksByProcessInstance", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByProcessDefinitionId(String processDefinitionId) {
    return getDbSqlSession().selectList("selectIdentityLinksByProcessDefinition", processDefinitionId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return getDbSqlSession().selectList("selectIdentityLinkByTaskUserGroupAndType", parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByProcessInstanceUserGroupAndType(String processInstanceId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return getDbSqlSession().selectList("selectIdentityLinkByProcessInstanceUserGroupAndType", parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByProcessDefinitionUserAndGroup(String processDefinitionId, String userId, String groupId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    return getDbSqlSession().selectList("selectIdentityLinkByProcessDefinitionUserAndGroup", parameters);
  }
  
  @Override
  public IdentityLinkEntity addIdentityLink(ExecutionEntity executionEntity, String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    executionEntity.getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessInstance(executionEntity.getProcessInstance() != null ? executionEntity.getProcessInstance() : executionEntity);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    insert(identityLinkEntity);
    return identityLinkEntity;
  }
  
  @Override
  public IdentityLinkEntity addIdentityLink(TaskEntity taskEntity, String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    taskEntity.getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(taskEntity);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    insert(identityLinkEntity);
    if (userId != null && taskEntity.getProcessInstanceId() != null) {
      involveUser(taskEntity.getProcessInstance(), userId, IdentityLinkType.PARTICIPANT);
    }
    return identityLinkEntity;
  }
  
  @Override
  public IdentityLinkEntity addIdentityLink(ProcessDefinitionEntity processDefinitionEntity, String userId, String groupId) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    processDefinitionEntity.getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessDef(processDefinitionEntity);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(IdentityLinkType.CANDIDATE);
    insert(identityLinkEntity);
    return identityLinkEntity;
  }
  
  /**
   * Adds an IdentityLink for the given user id with the specified type, 
   * but only if the user is not associated with the execution entity yet.
   **/
  @Override
  public IdentityLinkEntity involveUser(ExecutionEntity executionEntity, String userId, String type) {
    for (IdentityLinkEntity identityLink : executionEntity.getIdentityLinks()) {
      if (identityLink.isUser() && identityLink.getUserId().equals(userId)) {
        return identityLink;
      }
    }
    return addIdentityLink(executionEntity, userId, null, type);
  }
  
  @Override
  public void addCandidateUser(TaskEntity taskEntity, String userId) {
    addIdentityLink(taskEntity, userId, null, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void addCandidateUsers(TaskEntity taskEntity, Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(taskEntity, candidateUser);
    }
  }

  @Override
  public void addCandidateGroup(TaskEntity taskEntity, String groupId) {
    addIdentityLink(taskEntity, null, groupId, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void addCandidateGroups(TaskEntity taskEntity, Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(taskEntity, candidateGroup);
    }
  }

  @Override
  public void addGroupIdentityLink(TaskEntity taskEntity, String groupId, String identityLinkType) {
    addIdentityLink(taskEntity, null, groupId, identityLinkType);
  }

  @Override
  public void addUserIdentityLink(TaskEntity taskEntity, String userId, String identityLinkType) {
    addIdentityLink(taskEntity, userId, null, identityLinkType);
  }
  
  @Override
  public void deleteIdentityLink(ExecutionEntity executionEntity, String userId, String groupId, String type) {
    String id = executionEntity.getProcessInstanceId() != null ? executionEntity.getProcessInstanceId() : executionEntity.getId();
    List<IdentityLinkEntity> identityLinks = findIdentityLinkByProcessInstanceUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, true);
    }

    executionEntity.getIdentityLinks().removeAll(identityLinks);
  }
  
  @Override
  public void deleteIdentityLink(TaskEntity taskEntity, String userId, String groupId, String type) {
    List<IdentityLinkEntity> identityLinks = findIdentityLinkByTaskUserGroupAndType(taskEntity.getId(), userId, groupId, type);
    
    List<String> identityLinkIds = new ArrayList<String>();
    for (IdentityLinkEntity identityLink: identityLinks) {
      deleteIdentityLink(identityLink, true);
      identityLinkIds.add(identityLink.getId());
    }

    // fix deleteCandidate() in create TaskListener
    List<IdentityLinkEntity> removedIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
    for (IdentityLinkEntity identityLinkEntity : taskEntity.getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType()) && 
          identityLinkIds.contains(identityLinkEntity.getId()) == false) {
        
        if ((userId != null && userId.equals(identityLinkEntity.getUserId()))
          || (groupId != null && groupId.equals(identityLinkEntity.getGroupId()))) {
          
          deleteIdentityLink(identityLinkEntity, true);
          removedIdentityLinkEntities.add(identityLinkEntity);
          
        }
      }
    }
    
    taskEntity.getIdentityLinks().removeAll(removedIdentityLinkEntities);
  }
  
  @Override
  public void deleteIdentityLink(ProcessDefinitionEntity processDefinitionEntity, String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = getIdentityLinkEntityManager()
          .findIdentityLinkByProcessDefinitionUserAndGroup(processDefinitionEntity.getId(), userId, groupId);

    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
  }

  @Override
  public void deleteIdentityLinksByTaskId(String taskId) {
    List<IdentityLinkEntity> identityLinks = findIdentityLinksByTaskId(taskId);
    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
  }

  @Override
  public void deleteIdentityLinksByProcDef(String processDefId) {
    getDbSqlSession().delete("deleteIdentityLinkByProcDef", processDefId);
  }

}

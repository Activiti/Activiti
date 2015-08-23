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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.task.IdentityLinkType;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class IdentityLinkEntityManager extends AbstractEntityManager<IdentityLinkEntity> {
  
  @Override
  public void insert(IdentityLinkEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);
    Context.getCommandContext().getHistoryManager().recordIdentityLinkCreated(entity);
  }

  public void deleteIdentityLink(IdentityLinkEntity identityLink, boolean cascadeHistory) {
    getDbSqlSession().delete(identityLink);
    if (cascadeHistory) {
      getHistoryManager().deleteHistoricIdentityLink(identityLink.getId());
    }

    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, identityLink));
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
  public IdentityLinkEntity involveUser(ExecutionEntity executionEntity, String userId, String type) {
    for (IdentityLinkEntity identityLink : executionEntity.getIdentityLinks()) {
      if (identityLink.isUser() && identityLink.getUserId().equals(userId)) {
        return identityLink;
      }
    }
    return addIdentityLink(executionEntity, userId, null, type);
  }
  
  public void addCandidateUser(TaskEntity taskEntity, String userId) {
    addIdentityLink(taskEntity, userId, null, IdentityLinkType.CANDIDATE);
  }

  public void addCandidateUsers(TaskEntity taskEntity, Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(taskEntity, candidateUser);
    }
  }

  public void addCandidateGroup(TaskEntity taskEntity, String groupId) {
    addIdentityLink(taskEntity, null, groupId, IdentityLinkType.CANDIDATE);
  }

  public void addCandidateGroups(TaskEntity taskEntity, Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(taskEntity, candidateGroup);
    }
  }

  public void addGroupIdentityLink(TaskEntity taskEntity, String groupId, String identityLinkType) {
    addIdentityLink(taskEntity, null, groupId, identityLinkType);
  }

  public void addUserIdentityLink(TaskEntity taskEntity, String userId, String identityLinkType) {
    addIdentityLink(taskEntity, userId, null, identityLinkType);
  }
  
  public void deleteIdentityLink(ExecutionEntity executionEntity, String userId, String groupId, String type) {
    String id = executionEntity.getProcessInstanceId() != null ? executionEntity.getProcessInstanceId() : executionEntity.getId();
    List<IdentityLinkEntity> identityLinks = findIdentityLinkByProcessInstanceUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, true);
    }

    executionEntity.getIdentityLinks().removeAll(identityLinks);
  }
  
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
  
  public void deleteIdentityLink(ProcessDefinitionEntity processDefinitionEntity, String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = Context.getCommandContext().getIdentityLinkEntityManager()
          .findIdentityLinkByProcessDefinitionUserAndGroup(processDefinitionEntity.getId(), userId, groupId);

    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
  }

  public void deleteIdentityLinksByTaskId(String taskId) {
    List<IdentityLinkEntity> identityLinks = findIdentityLinksByTaskId(taskId);
    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, false);
    }
  }

  public void deleteIdentityLinksByProcInstance(String processInstanceId) {

    // Identity links from db
    List<IdentityLinkEntity> identityLinks = findIdentityLinksByProcessInstanceId(processInstanceId);
    // Delete
    for (IdentityLinkEntity identityLink : identityLinks) {
      deleteIdentityLink(identityLink, false);
    }

    // Identity links from cache, if not already deleted
    List<IdentityLinkEntity> identityLinksFromCache = Context.getCommandContext().getDbSqlSession().findInCache(IdentityLinkEntity.class);
    boolean alreadyDeleted = false;
    for (IdentityLinkEntity identityLinkEntity : identityLinksFromCache) {
      if (processInstanceId.equals(identityLinkEntity.getProcessInstanceId())) {
        alreadyDeleted = false;
        for (IdentityLinkEntity deleted : identityLinks) {
          if (deleted.getId() != null && deleted.getId().equals(identityLinkEntity.getId())) {
            alreadyDeleted = true;
            break;
          }
        }

        if (!alreadyDeleted) {
          deleteIdentityLink(identityLinkEntity, false);
        }
      }
    }
  }

  public void deleteIdentityLinksByProcDef(String processDefId) {
    getDbSqlSession().delete("deleteIdentityLinkByProcDef", processDefId);
  }

}

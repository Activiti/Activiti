/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.api.internal.Internal;

import java.util.Collection;
import java.util.List;


@Internal
public interface IdentityLinkEntityManager extends EntityManager<IdentityLinkEntity> {

  List<IdentityLinkEntity> findIdentityLinksByTaskId(String taskId);

  List<IdentityLinkEntity> findIdentityLinksByProcessInstanceId(String processInstanceId);

  List<IdentityLinkEntity> findIdentityLinksByProcessDefinitionId(String processDefinitionId);

  List<IdentityLinkEntity> findIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type);

  List<IdentityLinkEntity> findIdentityLinkByProcessInstanceUserGroupAndType(String processInstanceId, String userId, String groupId, String type);

  List<IdentityLinkEntity> findIdentityLinkByProcessDefinitionUserAndGroup(String processDefinitionId, String userId, String groupId);


  IdentityLinkEntity addIdentityLink(ExecutionEntity executionEntity, String userId, String groupId, String type);

  IdentityLinkEntity addIdentityLink(TaskEntity taskEntity, String userId, String groupId, String type);

  IdentityLinkEntity addIdentityLink(ProcessDefinitionEntity processDefinitionEntity, String userId, String groupId);

  /**
   * Adds an IdentityLink for the given user id with the specified type,
   * but only if the user is not associated with the execution entity yet.
   **/
  IdentityLinkEntity involveUser(ExecutionEntity executionEntity, String userId, String type);

  void addCandidateUser(TaskEntity taskEntity, String userId);

  void addCandidateUsers(TaskEntity taskEntity, Collection<String> candidateUsers);

  void addCandidateGroup(TaskEntity taskEntity, String groupId);

  void addCandidateGroups(TaskEntity taskEntity, Collection<String> candidateGroups);

  void addGroupIdentityLink(TaskEntity taskEntity, String groupId, String identityLinkType);

  void addUserIdentityLink(TaskEntity taskEntity, String userId, String identityLinkType);


  void deleteIdentityLink(IdentityLinkEntity identityLink, boolean cascadeHistory);

  void deleteIdentityLink(ExecutionEntity executionEntity, String userId, String groupId, String type);

  void deleteIdentityLink(TaskEntity taskEntity, String userId, String groupId, String type);

  void deleteIdentityLink(ProcessDefinitionEntity processDefinitionEntity, String userId, String groupId);

  void deleteIdentityLinksByTaskId(String taskId);

  void deleteIdentityLinksByProcDef(String processDefId);

}

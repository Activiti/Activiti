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

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;


public class AddIdentityLinkCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  public static int IDENTITY_USER = 1;
  public static int IDENTITY_GROUP = 2;

  protected String identityId;

  protected int identityIdType;

  protected String identityType;

  public AddIdentityLinkCmd(String taskId, String identityId, int identityIdType, String identityType) {
    super(taskId);
    validateParams(taskId, identityId, identityIdType, identityType);
    this.taskId = taskId;
    this.identityId = identityId;
    this.identityIdType = identityIdType;
    this.identityType = identityType;
  }

  protected void validateParams(String taskId, String identityId, int identityIdType, String identityType) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("taskId is null");
    }

    if (identityType == null) {
      throw new ActivitiIllegalArgumentException("type is required when adding a new task identity link");
    }

    if (identityId == null && (identityIdType == IDENTITY_GROUP ||
        (!IdentityLinkType.ASSIGNEE.equals(identityType) && !IdentityLinkType.OWNER.equals(identityType)))) {

      throw new ActivitiIllegalArgumentException("identityId is null");
    }

    if (identityIdType != IDENTITY_USER && identityIdType != IDENTITY_GROUP) {
      throw new ActivitiIllegalArgumentException("identityIdType allowed values are 1 and 2");
    }
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {

    boolean assignedToNoOne = false;
    if (IdentityLinkType.ASSIGNEE.equals(identityType)) {
      commandContext.getTaskEntityManager().changeTaskAssignee(task, identityId);
      assignedToNoOne = identityId == null;
    } else if (IdentityLinkType.OWNER.equals(identityType)) {
      commandContext.getTaskEntityManager().changeTaskOwner(task, identityId);
    } else if (IDENTITY_USER == identityIdType) {
      task.addUserIdentityLink(identityId, identityType);
    } else if (IDENTITY_GROUP == identityIdType) {
      task.addGroupIdentityLink(identityId, identityType);
    }

    boolean forceNullUserId = false;
    if (assignedToNoOne) {
      // ACT-1317: Special handling when assignee is set to NULL, a
      // CommentEntity notifying of assignee-delete should be created
      forceNullUserId = true;

    }

    if (IDENTITY_USER == identityIdType) {
      commandContext.getHistoryManager().createUserIdentityLinkComment(taskId, identityId, identityType, true, forceNullUserId);
    } else {
      commandContext.getHistoryManager().createGroupIdentityLinkComment(taskId, identityId, identityType, true);
    }

    return null;
  }

}

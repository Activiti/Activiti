/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;


public class DeleteIdentityLinkForProcessDefinitionCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;

  protected String userId;

  protected String groupId;

  public DeleteIdentityLinkForProcessDefinitionCmd(String processDefinitionId, String userId, String groupId) {
    validateParams(userId, groupId, processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    this.userId = userId;
    this.groupId = groupId;
  }

  protected void validateParams(String userId, String groupId, String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("processDefinitionId is null");
    }

    if (userId == null && groupId == null) {
      throw new ActivitiIllegalArgumentException("userId and groupId cannot both be null");
    }
  }

  public Void execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = commandContext.getProcessDefinitionEntityManager().findById(processDefinitionId);

    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Cannot find process definition with id " + processDefinitionId, ProcessDefinition.class);
    }
    executeInternal(commandContext,processDefinition);
    return null;
  }

  protected void executeInternal(CommandContext commandContext,ProcessDefinitionEntity processDefinition) {
      commandContext.getIdentityLinkEntityManager().deleteIdentityLink(processDefinition, userId, groupId);
  }
}

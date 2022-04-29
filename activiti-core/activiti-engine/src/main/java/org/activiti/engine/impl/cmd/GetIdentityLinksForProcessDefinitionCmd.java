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
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;


public class GetIdentityLinksForProcessDefinitionCmd implements Command<List<IdentityLink>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetIdentityLinksForProcessDefinitionCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<IdentityLink> execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = commandContext.getProcessDefinitionEntityManager().findById(processDefinitionId);

    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Cannot find process definition with id " + processDefinitionId, ProcessDefinition.class);
    }

    List<IdentityLink> identityLinks = (List) processDefinition.getIdentityLinks();
    return identityLinks;
  }

}

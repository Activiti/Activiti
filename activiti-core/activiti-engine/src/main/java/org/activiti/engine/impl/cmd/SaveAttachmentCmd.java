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

import java.io.Serializable;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.task.Attachment;


public class SaveAttachmentCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected Attachment attachment;

  public SaveAttachmentCmd(Attachment attachment) {
    this.attachment = attachment;
  }

  public Object execute(CommandContext commandContext) {
    AttachmentEntity updateAttachment = commandContext.getAttachmentEntityManager().findById(attachment.getId());

    String processInstanceId = updateAttachment.getProcessInstanceId();
    String processDefinitionId = null;
    if (updateAttachment.getProcessInstanceId() != null) {
      ExecutionEntity process = commandContext.getExecutionEntityManager().findById(processInstanceId);
      if (process != null) {
        processDefinitionId = process.getProcessDefinitionId();
      }
    }
    executeInternal(commandContext,updateAttachment,processInstanceId,processDefinitionId);
    return null;
  }

  protected void executeInternal(CommandContext commandContext, AttachmentEntity updateAttachment,String processInstanceId,String processDefinitionId){
      updateAttachment.setName(attachment.getName());
      updateAttachment.setDescription(attachment.getDescription());

      if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
          commandContext.getProcessEngineConfiguration().getEventDispatcher()
              .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, attachment, processInstanceId, processInstanceId, processDefinitionId));
      }
  }
}

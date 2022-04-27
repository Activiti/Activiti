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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.Task;


public class AddCommentCmd implements Command<Comment> {

  protected String taskId;
  protected String processInstanceId;
  protected String type;
  protected String message;

  public AddCommentCmd(String taskId, String processInstanceId, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.message = message;
  }

  public AddCommentCmd(String taskId, String processInstanceId, String type, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.type = type;
    this.message = message;
  }

  public Comment execute(CommandContext commandContext) {

    TaskEntity task = null;
    // Validate task
    if (taskId != null) {
      task = commandContext.getTaskEntityManager().findById(taskId);

      if (task == null) {
        throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId, Task.class);
      }

      if (task.isSuspended()) {
        throw new ActivitiException(getSuspendedTaskException());
      }
    }

    ExecutionEntity execution = null;
    if (processInstanceId != null) {
      execution = commandContext.getExecutionEntityManager().findById(processInstanceId);

      if (execution == null) {
        throw new ActivitiObjectNotFoundException("execution " + processInstanceId + " doesn't exist", Execution.class);
      }

      if (execution.isSuspended()) {
        throw new ActivitiException(getSuspendedExceptionMessage());
      }
    }

    String processDefinitionId = null;
    if (execution != null) {
      processDefinitionId = execution.getProcessDefinitionId();
    } else if (task != null) {
      processDefinitionId = task.getProcessDefinitionId();
    }

    return executeInternal(commandContext,processDefinitionId);
  }

  protected Comment executeInternal(CommandContext commandContext,String processDefinitionId){

      String userId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = commandContext.getCommentEntityManager().create();
      comment.setUserId(userId);
      comment.setType((type == null) ? CommentEntity.TYPE_COMMENT : type);
      comment.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
      comment.setTaskId(taskId);
      comment.setProcessInstanceId(processInstanceId);
      comment.setAction(Event.ACTION_ADD_COMMENT);

      String eventMessage = message.replaceAll("\\s+", " ");
      if (eventMessage.length() > 163) {
          eventMessage = eventMessage.substring(0, 160) + "...";
      }
      comment.setMessage(eventMessage);

      comment.setFullMessage(message);

      commandContext.getCommentEntityManager().insert(comment);

      return comment;
  }

  protected String getSuspendedTaskException() {
    return "Cannot add a comment to a suspended task";
  }

  protected String getSuspendedExceptionMessage() {
    return "Cannot add a comment to a suspended execution";
  }
}

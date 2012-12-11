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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.CommentManager;
import org.activiti.engine.task.Comment;

/**
 * @author Joram Barrez
 */
public class DeleteCommentCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String processInstanceId;

  public DeleteCommentCmd(String taskId, String processInstanceId) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
  }

  public Void execute(CommandContext commandContext) {
    CommentManager commentManager = commandContext.getCommentManager();
    
    ArrayList<Comment> comments = new ArrayList<Comment>();
    if (processInstanceId != null) {
      comments.addAll(commentManager.findCommentsByProcessInstanceId(processInstanceId));
    }
    if (taskId != null) {
      comments.addAll(commentManager.findCommentsByTaskId(taskId));
    }

    for (Comment comment : comments) {
      commentManager.delete((CommentEntity)comment);
    }
    
    return null;
  }
}

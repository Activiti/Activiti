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

package org.activiti.rest.service.api.runtime.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskCommentResource extends TaskBaseResource {

  @Get
  public CommentResponse getComment() {
    if(!authenticate())
      return null;
    
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    
    String commentId = getAttribute("commentId");
    if(commentId == null) {
      throw new ActivitiIllegalArgumentException("CommentId is required.");
    }
    
    Comment comment = ActivitiUtil.getTaskService().getComment(commentId);
    if(comment == null || !task.getId().equals(comment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestComment(this, comment);
  }
  
  @Delete
  public void deleteComment() {
    if(!authenticate())
      return;
    
    // Check if task exists
    Task task = getTaskFromRequest();
    
    String commentId = getAttribute("commentId");
    if(commentId == null) {
      throw new ActivitiIllegalArgumentException("CommentId is required.");
    }
    
    Comment comment = ActivitiUtil.getTaskService().getComment(commentId);
    if(comment == null || comment.getTaskId() == null || !comment.getTaskId().equals(task.getId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }
    
    ActivitiUtil.getTaskService().deleteComment(commentId);
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

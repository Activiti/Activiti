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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.CommentRequest;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;


/**
 * @author Frederik Heremans
 */
public class TaskCommentCollectionResource extends TaskBaseResource {

  @Get
  public List<CommentResponse> getComments() {
    if(!authenticate())
      return null;
    
    List<CommentResponse> result = new ArrayList<CommentResponse>();
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    String type = getAttribute("type");

    List<Comment> taskComments;
    if (StringUtils.isNotBlank(type)) {
      taskComments = ActivitiUtil.getTaskService().getTaskComments(task.getId(), type);
    } else {
      taskComments = ActivitiUtil.getTaskService().getTaskComments(task.getId());
    }
    for(Comment comment : taskComments) {
      result.add(responseFactory.createCommentResponse(this, comment));
    }
    
    return result;
  }
  
  @Post
  public CommentResponse createComment(CommentRequest comment) {
    if(!authenticate())
      return null;
    
    Task task = getTaskFromRequest();
    
    if(comment.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Comment text is required.");
    }

    TaskService taskService = ActivitiUtil.getTaskService();
    Task taskEntity = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    String processInstanceId = null;
    if (comment.isSaveProcessInstanceId()) {
      processInstanceId = taskEntity.getProcessInstanceId();
    }
    Comment createdComment = taskService.addComment(taskEntity.getId(), processInstanceId, comment.getType(), comment.getMessage());
    setStatus(Status.SUCCESS_CREATED);
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
           .createCommentResponse(this, createdComment);
  }
}

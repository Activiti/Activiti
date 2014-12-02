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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.CommentRequest;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskCommentCollectionResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/comments", method = RequestMethod.GET, produces="application/json")
  public List<CommentResponse> getComments(@PathVariable String taskId, HttpServletRequest request) {
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    return restResponseFactory.createRestCommentList(taskService.getTaskComments(task.getId()));
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/comments", method = RequestMethod.POST, produces="application/json")
  public CommentResponse createComment(@PathVariable String taskId, @RequestBody CommentRequest comment, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Task task = getTaskFromRequest(taskId);
    
    if (comment.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Comment text is required.");
    }
    
    String processInstanceId = null;
    if (comment.isSaveProcessInstanceId()) {
      Task taskEntity = taskService.createTaskQuery().taskId(task.getId()).singleResult();
      processInstanceId = taskEntity.getProcessInstanceId();
    }
    Comment createdComment = taskService.addComment(task.getId(), processInstanceId, comment.getMessage());
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createRestComment(createdComment);
  }
}

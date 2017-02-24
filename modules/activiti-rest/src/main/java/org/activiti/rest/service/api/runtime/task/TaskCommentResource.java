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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Tasks" }, description = "Manage Tasks", authorizations = { @Authorization(value = "basicAuth") })
public class TaskCommentResource extends TaskBaseResource {

  @ApiOperation(value = " Get a comment on a task", tags = {"Tasks"}, nickname = "getTaskComment")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the task and comment were found and the comment is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found or the tasks doesn’t have a comment with the given ID.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/comments/{commentId}", method = RequestMethod.GET, produces = "application/json")
  public CommentResponse getComment(@ApiParam(name = "taskId", value="The id of the task to get the comment for.") @PathVariable("taskId") String taskId,@ApiParam(name = "commentId", value="The id of the comment.") @PathVariable("commentId") String commentId, HttpServletRequest request) {

    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);

    Comment comment = taskService.getComment(commentId);
    if (comment == null || !task.getId().equals(comment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }

    return restResponseFactory.createRestComment(comment);
  }

  @ApiOperation(value = "Delete a comment on a task", tags = {"Tasks"}, nickname = "deleteTaskComment")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates the task and comment were found and the comment is deleted. Response body is left empty intentionally."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found or the tasks doesn’t have a comment with the given ID.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/comments/{commentId}", method = RequestMethod.DELETE)
  public void deleteComment(@ApiParam(name = "taskId", value="The id of the task to delete the comment for.") @PathVariable("taskId") String taskId,@ApiParam(name = "commentId", value="The id of the comment.") @PathVariable("commentId") String commentId, HttpServletResponse response) {

    // Check if task exists
    Task task = getTaskFromRequest(taskId);

    Comment comment = taskService.getComment(commentId);
    if (comment == null || comment.getTaskId() == null || !comment.getTaskId().equals(task.getId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }

    taskService.deleteComment(commentId);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

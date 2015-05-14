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

package org.activiti.rest.service.api.history;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class HistoricProcessInstanceCommentResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;
  
  @Autowired
  protected TaskService taskService;
  
  @RequestMapping(value="/history/historic-process-instances/{processInstanceId}/comments/{commentId}", method = RequestMethod.GET, produces = "application/json")
  public CommentResponse getComment(@PathVariable("processInstanceId") String processInstanceId, 
      @PathVariable("commentId") String commentId, HttpServletRequest request) {
     
    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
     
    Comment comment = taskService.getComment(commentId);
    if (comment == null || comment.getProcessInstanceId() == null || !comment.getProcessInstanceId().equals(instance.getId())) {
      throw new ActivitiObjectNotFoundException("Process instance '" + instance.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }
    
    return restResponseFactory.createRestComment(comment);
  }
  
  @RequestMapping(value="/history/historic-process-instances/{processInstanceId}/comments/{commentId}", method = RequestMethod.DELETE)
  public void deleteComment(@PathVariable("processInstanceId") String processInstanceId, 
      @PathVariable("commentId") String commentId, HttpServletRequest request, HttpServletResponse response) {
    
    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
    
    Comment comment = taskService.getComment(commentId);
    if (comment == null || comment.getProcessInstanceId() == null || !comment.getProcessInstanceId().equals(instance.getId())) {
      throw new ActivitiObjectNotFoundException("Process instance '" + instance.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
    }
    
    taskService.deleteComment(commentId);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
 protected HistoricProcessInstance getHistoricProcessInstanceFromRequest(String processInstanceId) {
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
           .processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", HistoricProcessInstance.class);
    }
    return processInstance;
  }
}

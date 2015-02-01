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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoricProcessInstanceCommentCollectionResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;
  
  @Autowired
  protected TaskService taskService;
  
  @RequestMapping(value="/history/historic-process-instances/{processInstanceId}/comments", method = RequestMethod.GET, produces = "application/json")
  public List<CommentResponse> getComments(@PathVariable String processInstanceId, HttpServletRequest request) {
    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
    return restResponseFactory.createRestCommentList(taskService.getProcessInstanceComments(instance.getId()));
  }
	
  @RequestMapping(value="/history/historic-process-instances/{processInstanceId}/comments", method = RequestMethod.POST, produces = "application/json")
  public CommentResponse createComment(@PathVariable String processInstanceId, @RequestBody CommentResponse comment, 
      HttpServletRequest request, HttpServletResponse response) {
    
    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
    
    if (comment.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Comment text is required.");
    }
    
    Comment createdComment = taskService.addComment(null, instance.getId(), comment.getMessage());
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createRestComment(createdComment);
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

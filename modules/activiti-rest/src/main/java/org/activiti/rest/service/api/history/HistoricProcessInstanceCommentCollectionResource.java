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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public class HistoricProcessInstanceCommentCollectionResource extends SecuredResource {

	 @Get
	  public List<CommentResponse> getComments() {
	    if(!authenticate())
	      return null;
	    
	    List<CommentResponse> result = new ArrayList<CommentResponse>();
	    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
	    
	    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest();
	    
	    for(Comment comment : ActivitiUtil.getTaskService().getProcessInstanceComments(instance.getId())) {
	      result.add(responseFactory.createRestComment(this, comment));
	    }
	    
	    return result;
	  }
	 
	 @Post
	  public CommentResponse createComment(CommentResponse comment) {
	    if(!authenticate())
	      return null;
	    
	    HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest();
	    
	    if(comment.getMessage() == null) {
	      throw new ActivitiIllegalArgumentException("Comment text is required.");
	    }
	    
	    Comment createdComment = ActivitiUtil.getTaskService().addComment(null, instance.getId(), comment.getMessage());
	    setStatus(Status.SUCCESS_CREATED);
	    
	    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
	           .createRestComment(this, createdComment);
	  }
	 
	 protected HistoricProcessInstance getHistoricProcessInstanceFromRequest() {
	    String processInstanceId = getAttribute("processInstanceId");
	    if (processInstanceId == null) {
	      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
	    }
	    
	    HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
	           .processInstanceId(processInstanceId).singleResult();
	    if (processInstance == null) {
	      throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", HistoricProcessInstance.class);
	    }
	    return processInstance;
	  }
}

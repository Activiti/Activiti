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
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.CommentResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
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
    
    for(Comment comment : ActivitiUtil.getTaskService().getTaskComments(task.getId())) {
      result.add(responseFactory.createRestComment(this, comment));
    }
    
    return result;
  }
  
  @Post
  public CommentResponse createComment(CommentResponse comment) {
    if(!authenticate())
      return null;
    
    Task task = getTaskFromRequest();
    
    if(comment.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Comment text is required.");
    }
    
    Comment createdComment = ActivitiUtil.getTaskService().addComment(task.getId(), null, comment.getMessage());
    setStatus(Status.SUCCESS_CREATED);
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
           .createRestComment(this, createdComment);
  }
}

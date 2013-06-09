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

package org.activiti.rest.api.runtime.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.EventResponse;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskEventResource extends TaskBaseResource {

  @Get
  public EventResponse getComment() {
    if(!authenticate())
      return null;
    
    Task task = getTaskFromRequest();
    
    String eventId = getAttribute("eventId");
    if(eventId == null) {
      throw new ActivitiIllegalArgumentException("EventId is required.");
    }
    
    Event event = ActivitiUtil.getTaskService().getEvent(eventId);
    if(event == null || !task.getId().equals(event.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + eventId + "'.", Event.class);
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createEventResponse(this, event);
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

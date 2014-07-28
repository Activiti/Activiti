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
import org.activiti.engine.task.Event;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.EventResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskEventResource extends TaskBaseResource {

  @Get
  public EventResponse getEvent() {
    if(!authenticate())
      return null;
    
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    
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
  public void deleteEvent() {
    if(!authenticate())
      return;
    
    // Check if task exists
    Task task = getTaskFromRequest();
    
    String eventId = getAttribute("eventId");
    if(eventId == null) {
      throw new ActivitiIllegalArgumentException("EventId is required.");
    }
    
    Event event = ActivitiUtil.getTaskService().getEvent(eventId);
    if(event == null || event.getTaskId() == null || !event.getTaskId().equals(task.getId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + event + "'.", Event.class);
    }
    
    ActivitiUtil.getTaskService().deleteComment(eventId);
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}

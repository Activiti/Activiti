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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.EventResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskEventResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/events/{eventId}", method = RequestMethod.GET, produces="application/json")
  public EventResponse getEvent(@PathVariable("taskId") String taskId, 
      @PathVariable("eventId") String eventId, HttpServletRequest request) {
    
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    
    Event event = taskService.getEvent(eventId);
    if (event == null || !task.getId().equals(event.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + eventId + "'.", Event.class);
    }
    
    return restResponseFactory.createEventResponse(event);
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/events/{eventId}", method = RequestMethod.DELETE)
  public void deleteEvent(@PathVariable("taskId") String taskId, 
      @PathVariable("eventId") String eventId, HttpServletResponse response) {
    
    // Check if task exists
    Task task = getTaskFromRequest(taskId);
    
    Event event = taskService.getEvent(eventId);
    if (event == null || event.getTaskId() == null || !event.getTaskId().equals(task.getId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + event + "'.", Event.class);
    }
    
    taskService.deleteComment(eventId);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.rest.service.api.engine.EventResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Tasks" }, description = "Manage Tasks", authorizations = { @Authorization(value = "basicAuth") })
public class TaskEventCollectionResource extends TaskBaseResource {

  @ApiOperation(value = "Get all events for a task", tags = {"Tasks"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the task was found and the events are returned."),
      @ApiResponse(code = 404, message = "Indicates the requested task was not found.")
  })
  @RequestMapping(value = "/runtime/tasks/{taskId}/events", method = RequestMethod.GET, produces = "application/json")
  public List<EventResponse> getEvents(@ApiParam(name = "taskId", value="The id of the task to get the events for.") @PathVariable String taskId, HttpServletRequest request) {
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    return restResponseFactory.createEventResponseList(taskService.getTaskEvents(task.getId()));
  }
}

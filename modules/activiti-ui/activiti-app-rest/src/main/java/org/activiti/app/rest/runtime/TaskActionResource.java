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
package org.activiti.app.rest.runtime;

import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.service.editor.ActivitiTaskActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class TaskActionResource {
  
  @Autowired
  protected ActivitiTaskActionService taskActionService;

  @RequestMapping(value = "/rest/tasks/{taskId}/action/complete", method = RequestMethod.PUT)
  @ResponseStatus(value = HttpStatus.OK)
  public void completeTask(@PathVariable String taskId) {
    taskActionService.completeTask(taskId);
  }

  @RequestMapping(value = "/rest/tasks/{taskId}/action/assign", method = RequestMethod.PUT)
  public TaskRepresentation assignTask(@PathVariable String taskId, @RequestBody ObjectNode requestNode) {
    return taskActionService.assignTask(taskId, requestNode);
  }

  @RequestMapping(value = "/rest/tasks/{taskId}/action/involve", method = RequestMethod.PUT, produces = "application/json")
  @ResponseStatus(value = HttpStatus.OK)
  public void involveUser(@PathVariable("taskId") String taskId, @RequestBody ObjectNode requestNode) {
    taskActionService.involveUser(taskId, requestNode);
  }

  @RequestMapping(value = "/rest/tasks/{taskId}/action/remove-involved", method = RequestMethod.PUT, produces = "application/json")
  @ResponseStatus(value = HttpStatus.OK)
  public void removeInvolvedUser(@PathVariable("taskId") String taskId, @RequestBody ObjectNode requestNode) {
    taskActionService.removeInvolvedUser(taskId, requestNode);
  }

  @RequestMapping(value = "/rest/tasks/{taskId}/action/claim", method = RequestMethod.PUT)
  @ResponseStatus(value = HttpStatus.OK)
  public void claimTask(@PathVariable String taskId) {
    taskActionService.claimTask(taskId);
  }

}

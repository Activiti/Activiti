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
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Tijs Rademakers
 */
@RestController
public class HistoricTaskInstanceResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;

  @RequestMapping(value="/history/historic-task-instances/{taskId}", method = RequestMethod.GET, produces = "application/json")
  public HistoricTaskInstanceResponse getTaskInstance(@PathVariable String taskId, HttpServletRequest request) {
    return restResponseFactory.createHistoricTaskInstanceResponse(getHistoricTaskInstanceFromRequest(taskId));
  }
  
  @RequestMapping(value="/history/historic-task-instances/{taskId}", method = RequestMethod.DELETE)
  public void deleteTaskInstance(@PathVariable String taskId, HttpServletResponse response) {
    historyService.deleteHistoricTaskInstance(taskId);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  protected HistoricTaskInstance getHistoricTaskInstanceFromRequest(String taskId) {
    HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery()
           .taskId(taskId).singleResult();
    if (taskInstance == null) {
      throw new ActivitiObjectNotFoundException("Could not find a task instance with id '" + taskId + "'.", HistoricTaskInstance.class);
    }
    return taskInstance;
  }
}

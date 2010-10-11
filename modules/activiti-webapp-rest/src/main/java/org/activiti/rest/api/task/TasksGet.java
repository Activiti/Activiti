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
package org.activiti.rest.api.task;

import java.util.Map;

import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.task.TaskQueryProperty;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.springframework.extensions.webscripts.*;

/**
 * Returns info about a list of tasks depending on the search filters.
 *
 * @author Erik Winlof
 */
public class TasksGet extends ActivitiPagingWebScript
{

  public TasksGet() {
    properties.put("id", TaskQueryProperty.TASK_ID);
    properties.put("name", TaskQueryProperty.NAME);
    properties.put("description", TaskQueryProperty.DESCRIPTION);
    properties.put("priority", TaskQueryProperty.PRIORITY);
    properties.put("assignee", TaskQueryProperty.ASSIGNEE);
    properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
  }

  /**
   * Collects info about a list of tasks depending on the search filters for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String personalTaskUserId = req.getString("assignee");
    String candidateTaskUserId = req.getString("candidate");
    String candidateGroupId = req.getString("candidate-group");
    TaskQuery taskQuery = getTaskService().createTaskQuery();
    if (personalTaskUserId != null) {
      taskQuery.assignee(personalTaskUserId);
    }
    else if (candidateTaskUserId != null) {
      taskQuery.candidateUser(candidateTaskUserId);
    }
    else if (candidateGroupId != null) {
      taskQuery.candidateGroup(candidateGroupId);
    }
    else {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Tasks must be filtered with 'assignee', 'candidate' or 'candidate-group'");
    }
    paginateList(req, taskQuery, "tasks", model, "id"); 
  }

}

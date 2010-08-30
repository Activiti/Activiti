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
package org.activiti.rest.api.tasks;

import java.util.Map;

import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.*;

/**
 * Returns info about a list of tasks depending on the search filters.
 *
 * @author Erik Winl�f
 */
public class TasksGet extends ActivitiWebScript
{

  /**
   * Collects info about a list of tasks depending on the search filters for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String personalTaskUserId = getString(req, "assignee");
    String candidateTaskUserId = getString(req, "candidate");
    String candidateGroupId = getString(req, "candidate-group");
    int start = getInt(req, "start", 0);
    int size = getInt(req, "size", 10);
    TaskQuery tq = getTaskService().createTaskQuery();
    if (personalTaskUserId != null) {
      tq.assignee(personalTaskUserId);
    }
    else if (candidateTaskUserId != null) {
      tq.candidateUser(candidateTaskUserId);
    }
    else if (candidateGroupId != null) {
      tq.candidateGroup(candidateGroupId);
    }
    else {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Tasks must be filtered with 'assignee', 'candidate' or 'candidate-group'");
    }
    model.put("tasks", tq.listPage(start, size));
    model.put("start", start);
    model.put("total", tq.count());
    model.put("size", size);
  }

}

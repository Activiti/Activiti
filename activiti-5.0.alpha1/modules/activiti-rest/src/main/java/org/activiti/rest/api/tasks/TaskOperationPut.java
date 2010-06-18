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

import org.activiti.Task;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Performs a given action on a task.
 *
 * @author Erik Winlšf
 */
public class TaskOperationPut extends ActivitiWebScript
{

  /**
   * Performs a given action on a task and collects info about the task for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String taskId = getMandatoryPathParameter(req, "taskId");
    String operation = getMandatoryPathParameter(req, "operation");
    String currentUserId = getCurrentUserId(req);
    boolean result = false;
    if ("start".equals(operation)) {
      throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Not implemented in this version");
    }
    else if ("stop".equals(operation)) {
      throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Not implemented in this version");
    }
    else if ("claim".equals(operation)) {
      getTaskService().claim(taskId, currentUserId);
      result = true;
    }
    else if ("complete".equals(operation)) {
        getTaskService().complete(taskId);
        result = true;
    }
    else if ("revoke".equals(operation)) {
      throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Not implemented in this version");
    }
    else if ("suspend".equals(operation)) {
      throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Not implemented in this version");
    }
    else if ("resume".equals(operation)) {
      throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Not implemented in this version");
    }
    else
    {
      throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'" + operation + "' is not a valid operation");
    }
    model.put("result", result);
  }

}

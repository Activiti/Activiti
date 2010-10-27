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
package org.activiti.rest.api.management;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns signalData, metadata and paging info about a table.
 *
 * @author Erik Winlof
 */
public class JobsGet extends ActivitiPagingWebScript
{

  public JobsGet() {
    properties.put("id", JobQueryProperty.JOB_ID);
    properties.put("executionId", JobQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", JobQueryProperty.PROCESS_INSTANCE_ID);
    properties.put("dueDate", JobQueryProperty.DUEDATE);
    properties.put("retries", JobQueryProperty.RETRIES);
  }

  /**
   * Prepares signalData, metadata and paging info about a table for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String processInstanceId = req.getString("process-instance", null);
    Boolean withRetriesLeft = req.getBoolean("with-retries-left", false);
    Boolean executable = req.getBoolean("executable", false);
    Boolean onlyTimers = req.getBoolean("only-timers", false);
    Boolean onlyMessages = req.getBoolean("only-messages", false);
    Date dueDateLowerThen = req.getDate("duedate-lt", null);
    Date dueDateLowerThenOrEquals = req.getDate("duedate-ltoe", null);
    Date dueDateHigherThen = req.getDate("duedate-ht", null);
    Date dueDateHigherThenOrEquals = req.getDate("duedate-htoe", null);

    JobQuery jobQuery = getManagementService().createJobQuery();
    if (processInstanceId != null)
    {
      jobQuery.processInstanceId(processInstanceId);
    }
    if (withRetriesLeft)
    {
      jobQuery.withRetriesLeft();
    }
    if (executable)
    {
      jobQuery.executable();
    }
    if (onlyTimers)
    {
      jobQuery.onlyTimers();
    }
    if (onlyMessages)
    {
      jobQuery.onlyMessages();
    }
    if (dueDateLowerThen != null)
    {
      jobQuery.duedateLowerThen(dueDateLowerThen);
    }
    if (dueDateLowerThenOrEquals != null)
    {
      jobQuery.duedateLowerThenOrEquals(dueDateLowerThenOrEquals);
    }
    if (dueDateHigherThen != null)
    {
      jobQuery.duedateHigherThen(dueDateHigherThen);
    }
    if (dueDateHigherThenOrEquals != null)
    {
      jobQuery.duedateLowerThenOrEquals(dueDateLowerThenOrEquals);
    }

    paginateList(req, jobQuery, "jobs", model, "id");
  }

}

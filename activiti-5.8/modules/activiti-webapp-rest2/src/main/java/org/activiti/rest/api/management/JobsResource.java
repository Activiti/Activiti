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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class JobsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public JobsResource() {
    properties.put("id", JobQueryProperty.JOB_ID);
    properties.put("executionId", JobQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", JobQueryProperty.PROCESS_INSTANCE_ID);
    properties.put("dueDate", JobQueryProperty.DUEDATE);
    properties.put("retries", JobQueryProperty.RETRIES);
  }
  
  @Get
  public DataResponse getJobs() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    
    String processInstanceId = getQuery().getValues("process-instance");
    Boolean withRetriesLeft = RequestUtil.getBoolean(getQuery(), "with-retries-left", false);
    Boolean executable = RequestUtil.getBoolean(getQuery(), "executable", false);
    Boolean onlyTimers = RequestUtil.getBoolean(getQuery(), "only-timers", false);
    Boolean onlyMessages = RequestUtil.getBoolean(getQuery(), "only-messages", false);
    Date dueDateLowerThen = RequestUtil.getDate(getQuery(), "duedate-lt");
    Date dueDateLowerThenOrEquals = RequestUtil.getDate(getQuery(), "duedate-ltoe");
    Date dueDateHigherThen = RequestUtil.getDate(getQuery(), "duedate-ht");
    Date dueDateHigherThenOrEquals = RequestUtil.getDate(getQuery(), "duedate-htoe");

    JobQuery jobQuery = ActivitiUtil.getManagementService().createJobQuery();
    if (processInstanceId != null) {
      jobQuery.processInstanceId(processInstanceId);
    }
    if (withRetriesLeft) {
      jobQuery.withRetriesLeft();
    }
    if (executable) {
      jobQuery.executable();
    }
    if (onlyTimers) {
      jobQuery.timers();
    }
    if (onlyMessages) {
      jobQuery.messages();
    }
    if (dueDateLowerThen != null) {
      jobQuery.duedateLowerThen(dueDateLowerThen);
    }
    if (dueDateLowerThenOrEquals != null) {
      jobQuery.duedateLowerThenOrEquals(dueDateLowerThenOrEquals);
    }
    if (dueDateHigherThen != null) {
      jobQuery.duedateHigherThen(dueDateHigherThen);
    }
    if (dueDateHigherThenOrEquals != null) {
      jobQuery.duedateLowerThenOrEquals(dueDateLowerThenOrEquals);
    }
    
    DataResponse response = new JobsPaginateList().paginateList(getQuery(), jobQuery, "id", properties);
    return response;
  }
}
